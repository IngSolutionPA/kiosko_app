package com.example.kioskopda.kiosk

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserManager
import com.example.kioskopda.admin.KioskDeviceAdminReceiver

class KioskManager(private val context: Context) {
    private val dpm: DevicePolicyManager? = context.getSystemService(DevicePolicyManager::class.java)
    private val adminComponent = ComponentName(context, KioskDeviceAdminReceiver::class.java)

    fun isDeviceOwner(): Boolean = dpm?.isDeviceOwnerApp(context.packageName) == true

    fun isAdminActive(): Boolean = dpm?.isAdminActive(adminComponent) == true

    fun canUseFullKiosk(): Boolean = isDeviceOwner() && isAdminActive()

    fun requestEnableDeviceAdminIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Se requiere administrador para reforzar el modo kiosko."
            )
        }
    }

    fun applyKioskPolicies(allowedPackages: Set<String>) {
        if (!canUseFullKiosk()) return

        // Incluimos el paquete de ajustes del sistema para permitir el panel WiFi
        val systemSettingsPackages = setOf(
            "com.android.settings",
            "com.hihonor.android.settings",   // Honor/HarmonyOS settings
            "com.android.systemui"
        )
        val lockTaskPackages = (allowedPackages + context.packageName + systemSettingsPackages)
            .toTypedArray()
        dpm?.setLockTaskPackages(adminComponent, lockTaskPackages)
        grantPhoneStatePermission()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Permitimos acciones globales (panel WiFi y similares) pero mantenemos el resto bloqueado
            dpm?.setLockTaskFeatures(
                adminComponent,
                DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
            )
        }

        dpm?.setStatusBarDisabled(adminComponent, true)
        dpm?.setKeyguardDisabled(adminComponent, true)

        val restrictions = listOf(
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
            UserManager.DISALLOW_ADJUST_VOLUME
        )

        restrictions.forEach { restriction ->
            dpm?.addUserRestriction(adminComponent, restriction)
        }
    }

    fun grantPhoneStatePermission() {
        if (!canUseFullKiosk()) return

        runCatching {
            dpm?.setPermissionGrantState(
                adminComponent,
                context.packageName,
                Manifest.permission.READ_PHONE_STATE,
                DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
            )
        }
    }

    fun startLockTask(activity: Activity) {
        if (!canUseFullKiosk()) return
        runCatching {
            activity.startLockTask()
        }
    }

    fun stopLockTask(activity: Activity) {
        if (!canUseFullKiosk()) return
        runCatching {
            activity.stopLockTask()
        }
    }

    /**
     * Remueve el Device Owner desde dentro de la app.
     * Solo funciona si la app está configurada como Device Owner.
     */
    fun removeDeviceOwner(): Boolean {
        return runCatching {
            dpm?.clearDeviceOwnerApp(context.packageName)
            true
        }.getOrNull() == true
    }

    /**
     * Verifica nuevamente el estado de Device Owner.
     * Útil después de ejecutar comandos ADB.
     */
    fun refreshStatus() {
        // Fuerza la re-evaluación de los estados
        dpm?.let {
            it.isDeviceOwnerApp(context.packageName)
            it.isAdminActive(adminComponent)
        }
    }

    /**
     * Reinicia el dispositivo (confirmado funcional con Device Owner).
     */
    fun rebootDevice(activity: Activity) {
        runCatching { activity.stopLockTask() }
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            runCatching { dpm?.reboot(adminComponent) }
        }, 300L)
    }

    /**
     * Bloquea la pantalla inmediatamente (equivale a apagar la pantalla y asegurar el dispositivo).
     */
    fun lockScreen() {
        runCatching { dpm?.lockNow() }
    }
    /**
     * Intenta apagar el dispositivo. En la práctica, en Android no hay API
     * oficial para esto sin permisos de sistema. Se recomienda usar el
     * botón físico de encendido para apagar completamente.
     */
    fun powerOffDevice(activity: Activity): Boolean {
        // Paso 1: quitar TODAS las restricciones para que el sistema pueda responder
        runCatching {
            dpm?.setStatusBarDisabled(adminComponent, false)
            dpm?.setKeyguardDisabled(adminComponent, false)
        }

        // Paso 2: salir de Lock Task Mode
        runCatching { activity.stopLockTask() }

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({

            // Intento 1: IPowerManager.shutdown() vía binder (misma ruta interna que dpm.reboot())
            var done = false
            runCatching {
                val smClass = Class.forName("android.os.ServiceManager")
                val binder = smClass.getMethod("getService", String::class.java)
                    .invoke(null, "power") as? android.os.IBinder
                if (binder != null) {
                    val data  = android.os.Parcel.obtain()
                    val reply = android.os.Parcel.obtain()
                    data.writeInterfaceToken("android.os.IPowerManager")
                    data.writeInt(0)     // confirm = false
                    data.writeString(null) // reason = null → shutdown
                    data.writeInt(0)     // wait = false
                    // El código de transacción 13 corresponde a shutdown() en IPowerManager AIDL
                    binder.transact(13, data, reply, 0)
                    data.recycle()
                    reply.recycle()
                    done = true
                }
            }

            // Intento 2: intent interno del sistema (funciona sin lock task)
            if (!done) {
                runCatching {
                    val intent = Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN").apply {
                        putExtra("android.intent.extra.KEY_CONFIRM", false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    context.startActivity(intent)
                    done = true
                }
            }

            // Intento 3: PowerManager.shutdown() reflection
            if (!done) {
                runCatching {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                    pm.javaClass.getMethod("shutdown",
                        Boolean::class.javaPrimitiveType,
                        String::class.java,
                        Boolean::class.javaPrimitiveType
                    ).invoke(pm, false, null, false)
                }
            }

        }, 400L)

        return true
    }
}
