package com.example.kioskopda

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.addCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.kioskopda.kiosk.KioskConfig
import com.example.kioskopda.kiosk.KioskManager
import com.example.kioskopda.ui.theme.KioskoPDATheme
import com.example.kioskopda.ui.screens.KioskScreen

class MainActivity : ComponentActivity() {
    private lateinit var kioskManager: KioskManager
    private lateinit var deviceIdentifierProvider: DeviceIdentifierProvider
    private var deviceIdentifier by mutableStateOf<DeviceIdentifier?>(null)

    private val adminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!kioskManager.isAdminActive()) {
            Toast.makeText(this, getString(R.string.admin_not_enabled), Toast.LENGTH_SHORT).show()
        }
    }

    private val phoneStatePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        refreshDeviceIdentifier()

        if (!isGranted) {
            Toast.makeText(this, getString(R.string.imei_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kioskManager = KioskManager(this)
        deviceIdentifierProvider = DeviceIdentifierProvider(this)
        refreshDeviceIdentifier()

        enableEdgeToEdge()
        applyImmersiveMode()

        onBackPressedDispatcher.addCallback(this) {
            // Consumimos back para que no se cierre la actividad kiosko.
        }

        setContent {
            KioskoPDATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KioskScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        deviceIdentifier = deviceIdentifier,
                        kioskManager = kioskManager,
                        onRequestAdmin = {
                            adminLauncher.launch(kioskManager.requestEnableDeviceAdminIntent())
                        },
                        onExitKiosk = {
                            kioskManager.stopLockTask(this)
                            Toast.makeText(this, getString(R.string.kiosk_exit_success), Toast.LENGTH_SHORT).show()
                        },
                        onUninstall = {
                            if (kioskManager.removeDeviceOwner()) {
                                Toast.makeText(this, getString(R.string.uninstall_success), Toast.LENGTH_SHORT).show()
                                // Dar tiempo para que el toast se vea
                                window.decorView.postDelayed({
                                    startActivity(Intent(Intent.ACTION_DELETE).apply {
                                        data = android.net.Uri.parse("package:${packageName}")
                                    })
                                }, 1500)
                            } else {
                                Toast.makeText(this, "No se pudo remover Device Owner", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRefreshStatus = {
                            kioskManager.refreshStatus()
                            // Re-crear el composable para actualizar estados
                            window.decorView.postDelayed({
                                recreate()
                            }, 500)
                        },
                        onEnableDeviceOwner = {
                            // Mostrar instrucciones
                            showDeviceOwnerInstructions()
                        }
                    )
                }
            }
        }

        ensurePhoneStatePermission()
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveMode()
        if (kioskManager.canUseFullKiosk()) {
            kioskManager.applyKioskPolicies(KioskConfig.allowedPackages)
            kioskManager.startLockTask(this)
        }

        refreshDeviceIdentifier()
    }

    override fun onDestroy() {
        // Liberar lock task para que ADB/Android Studio pueda reinstalar sin errores
        runCatching { stopLockTask() }
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveMode()
        }
    }

    private fun applyImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun ensurePhoneStatePermission() {
        if (!deviceIdentifierProvider.hasTelephonyFeature()) {
            refreshDeviceIdentifier()
            return
        }

        if (deviceIdentifierProvider.hasPhoneStatePermission()) {
            refreshDeviceIdentifier()
            return
        }

        if (kioskManager.canUseFullKiosk()) {
            kioskManager.grantPhoneStatePermission()
            refreshDeviceIdentifier()
            return
        }

        phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
    }

    private fun refreshDeviceIdentifier() {
        deviceIdentifier = deviceIdentifierProvider.load()
    }

    private fun showDeviceOwnerInstructions() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val command = "adb shell dpm set-device-owner com.example.kioskopda/.admin.KioskDeviceAdminReceiver"

        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.device_owner_instructions_title))
            .setMessage(getString(R.string.device_owner_instructions))
            .setPositiveButton(getString(R.string.copy_command)) { _, _ ->
                val clip = android.content.ClipData.newPlainText("Device Owner Command", command)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.command_copied), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}


