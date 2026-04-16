package com.example.kioskopda

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.kioskopda.admin.KioskDeviceAdminReceiver

enum class DeviceIdentifierSource {
    IMEI,
    ANDROID_ID,
    UNAVAILABLE
}

data class DeviceIdentifier(
    val imei: String?,
    val value: String,
    val source: DeviceIdentifierSource
)

object DeviceIdentityStore {
    var current: DeviceIdentifier? = null
        private set

    val imei: String?
        get() = current?.imei

    fun update(identifier: DeviceIdentifier) {
        current = identifier
    }
}

class DeviceIdentifierProvider(context: Context) {
    private val appContext = context.applicationContext

    fun hasPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasTelephonyFeature(): Boolean {
        return appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    @SuppressLint("HardwareIds")
    fun load(): DeviceIdentifier {
        val imei = readImeiOrNull()

        val identifier = when {
            !imei.isNullOrBlank() -> DeviceIdentifier(
                imei = imei,
                value = imei,
                source = DeviceIdentifierSource.IMEI
            )

            else -> {
                val androidId = Settings.Secure.getString(
                    appContext.contentResolver,
                    Settings.Secure.ANDROID_ID
                )?.takeUnless { it.isBlank() || it == "9774d56d682e549c" }

                if (!androidId.isNullOrBlank()) {
                    DeviceIdentifier(
                        imei = null,
                        value = androidId,
                        source = DeviceIdentifierSource.ANDROID_ID
                    )
                } else {
                    DeviceIdentifier(
                        imei = null,
                        value = "",
                        source = DeviceIdentifierSource.UNAVAILABLE
                    )
                }
            }
        }

        DeviceIdentityStore.update(identifier)
        return identifier
    }

    private fun readImeiOrNull(): String? {
        if (!hasTelephonyFeature()) return null

        // Método 1: DevicePolicyManager.getImei() — solo disponible para Device Owner (API 26+).
        // No requiere READ_PRIVILEGED_PHONE_STATE, es la forma correcta en Android 10+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                val dpm = appContext.getSystemService(DevicePolicyManager::class.java)
                val admin = ComponentName(appContext, KioskDeviceAdminReceiver::class.java)
                if (dpm != null && dpm.isDeviceOwnerApp(appContext.packageName)) {
                    val imei = DevicePolicyManager::class.java
                        .getMethod("getImei", ComponentName::class.java, String::class.java)
                        .invoke(dpm, admin, null) as? String
                    if (!imei.isNullOrBlank()) return imei.trim()
                }
            }
        }

        // Método 2: TelephonyManager — requiere READ_PHONE_STATE (funciona hasta Android 9).
        if (!hasPhoneStatePermission()) return null

        val telephonyManager = appContext.getSystemService(TelephonyManager::class.java) ?: return null

        val identifier = runCatching {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    TelephonyManager::class.java
                        .getMethod("getImei")
                        .invoke(telephonyManager) as? String
                }

                else -> {
                    @Suppress("DEPRECATION")
                    TelephonyManager::class.java
                        .getMethod("getDeviceId")
                        .invoke(telephonyManager) as? String
                }
            }
        }.getOrNull()

        return identifier?.trim()?.takeUnless { it.isBlank() }
    }
}
