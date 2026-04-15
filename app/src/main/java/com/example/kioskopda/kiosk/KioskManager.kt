package com.example.kioskopda.kiosk

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

        val lockTaskPackages = (allowedPackages + context.packageName).toTypedArray()
        dpm?.setLockTaskPackages(adminComponent, lockTaskPackages)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            dpm?.setLockTaskFeatures(adminComponent, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
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
}

