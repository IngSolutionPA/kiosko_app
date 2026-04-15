package com.example.kioskopda.provisioning

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Build
import android.os.Bundle

@SuppressLint("NewApi")
@Suppress("DEPRECATION")
class ProvisioningActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            DevicePolicyManager.ACTION_GET_PROVISIONING_MODE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val result = Intent().putExtra(
                        DevicePolicyManager.EXTRA_PROVISIONING_MODE,
                        DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE
                    )
                    setResult(RESULT_OK, result)
                } else {
                    setResult(RESULT_OK)
                }
            }

            DevicePolicyManager.ACTION_ADMIN_POLICY_COMPLIANCE,
            DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE -> {
                setResult(RESULT_OK)
            }

            else -> {
                setResult(RESULT_CANCELED)
            }
        }

        finish()
    }
}

