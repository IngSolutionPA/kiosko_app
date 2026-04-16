package com.example.kioskopda.admin

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver de emergencia para desarrollo.
 * Permite remover el Device Owner via ADB:
 *   adb shell am broadcast -a com.example.kioskopda.CLEAR_DEVICE_OWNER -n com.example.kioskopda/.admin.AdbUnlockReceiver
 */
class AdbUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        runCatching {
            val dpm = context.getSystemService(DevicePolicyManager::class.java)
            val admin = ComponentName(context, KioskDeviceAdminReceiver::class.java)
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                dpm.clearDeviceOwnerApp(context.packageName)
                Log.i("AdbUnlockReceiver", "Device Owner removido correctamente")
            } else if (dpm.isAdminActive(admin)) {
                dpm.removeActiveAdmin(admin)
                Log.i("AdbUnlockReceiver", "Device Admin removido correctamente")
            }
        }.onFailure {
            Log.e("AdbUnlockReceiver", "Error al remover admin: ${it.message}")
        }
    }

    companion object {
        const val ACTION = "com.example.kioskopda.CLEAR_DEVICE_OWNER"
    }
}

