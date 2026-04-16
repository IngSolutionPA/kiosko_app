package com.example.kioskopda

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.addCallback
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.kioskopda.kiosk.KioskConfig
import com.example.kioskopda.kiosk.KioskManager
import com.example.kioskopda.ui.theme.KioskoPDATheme

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
}

@Composable
fun KioskScreen(
    modifier: Modifier = Modifier,
    deviceIdentifier: DeviceIdentifier?,
    kioskManager: KioskManager,
    onRequestAdmin: () -> Unit,
    onExitKiosk: () -> Unit,
    onUninstall: () -> Unit
) {
    val context = LocalContext.current
    val apps by rememberAllowedApps(allowedPackages = KioskConfig.allowedPackages)

    var showPinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = context.getString(R.string.kiosk_title))

        Text(
            text = if (kioskManager.canUseFullKiosk()) {
                context.getString(R.string.kiosk_status_full)
            } else {
                context.getString(R.string.kiosk_status_limited)
            }
        )

        Text(text = context.getString(R.string.device_identifier_label))
        Text(
            text = when (deviceIdentifier?.source) {
                DeviceIdentifierSource.IMEI -> deviceIdentifier.value
                DeviceIdentifierSource.ANDROID_ID -> context.getString(
                    R.string.device_identifier_fallback_android_id,
                    deviceIdentifier.value
                )

                DeviceIdentifierSource.UNAVAILABLE -> context.getString(R.string.device_identifier_unavailable)
                null -> context.getString(R.string.device_identifier_loading)
            }
        )

        if (!kioskManager.isAdminActive()) {
            Button(onClick = onRequestAdmin) {
                Text(text = context.getString(R.string.enable_admin))
            }
        }

        Text(text = context.getString(R.string.allowed_apps_label))

        if (apps.isEmpty()) {
            Text(text = context.getString(R.string.no_allowed_apps))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps) { app ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val launchIntent = context.packageManager
                                .getLaunchIntentForPackage(app.packageName)
                                ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        }
                    ) {
                        Text(text = app.label)
                    }
                }
            }
        }

        TextButton(
            modifier = Modifier.height(48.dp),
            onClick = { showPinDialog = true }
        ) {
            Text(text = context.getString(R.string.exit_kiosk))
        }

        TextButton(
            modifier = Modifier.height(48.dp),
            onClick = { onUninstall() }
        ) {
            Text(text = context.getString(R.string.uninstall_device_owner))
        }
    }

    if (showPinDialog) {
        ExitPinDialog(
            onDismiss = { showPinDialog = false },
            onPinOk = {
                showPinDialog = false
                onExitKiosk()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun KioskPreview() {
    KioskoPDATheme {
        KioskScreen(
            deviceIdentifier = DeviceIdentifier(
                imei = "359881234567890",
                value = "359881234567890",
                source = DeviceIdentifierSource.IMEI
            ),
            kioskManager = KioskManager(LocalContext.current),
            onRequestAdmin = {},
            onExitKiosk = {},
            onUninstall = {}
        )
    }
}

private data class LaunchableApp(
    val packageName: String,
    val label: String
)

@Composable
private fun rememberAllowedApps(allowedPackages: Set<String>): androidx.compose.runtime.State<List<LaunchableApp>> {
    val context = LocalContext.current
    return produceState(initialValue = emptyList(), allowedPackages) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        value = resolved
            .asSequence()
            .filter { it.activityInfo.packageName in allowedPackages }
            .map {
                LaunchableApp(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}

@Composable
private fun ExitPinDialog(onDismiss: () -> Unit, onPinOk: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = context.getString(R.string.pin_dialog_title)) },
        text = {
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    pin = it
                    showError = false
                },
                visualTransformation = PasswordVisualTransformation(),
                isError = showError,
                singleLine = true,
                label = { Text(text = context.getString(R.string.pin_dialog_label)) }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (pin == KioskConfig.exitPin) {
                    onPinOk()
                } else {
                    showError = true
                    Toast.makeText(context, context.getString(R.string.pin_invalid), Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = context.getString(R.string.accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = context.getString(R.string.cancel))
            }
        }
    )
}
