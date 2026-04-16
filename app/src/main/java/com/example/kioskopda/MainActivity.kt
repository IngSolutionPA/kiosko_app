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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.kioskopda.kiosk.KioskConfig
import com.example.kioskopda.kiosk.KioskManager
import com.example.kioskopda.ui.theme.KioskoPDATheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@Composable
fun KioskScreen(
    modifier: Modifier = Modifier,
    deviceIdentifier: DeviceIdentifier?,
    kioskManager: KioskManager,
    onRequestAdmin: () -> Unit,
    onExitKiosk: () -> Unit,
    onUninstall: () -> Unit,
    onRefreshStatus: () -> Unit,
    onEnableDeviceOwner: () -> Unit
) {
    val context = LocalContext.current
    val apps by rememberAllowedApps(allowedPackages = KioskConfig.allowedPackages)
    var showPinDialog by remember { mutableStateOf(false) }

    val visibleAppSlots = remember(apps) { List(4) { index -> apps.getOrNull(index) } }
    val imeiText = remember(deviceIdentifier) {
        when (deviceIdentifier?.source) {
            DeviceIdentifierSource.IMEI -> context.getString(
                R.string.dashboard_imei_format,
                deviceIdentifier.value
            )

            DeviceIdentifierSource.ANDROID_ID -> context.getString(
                R.string.device_identifier_fallback_android_id,
                deviceIdentifier.value
            )

            DeviceIdentifierSource.UNAVAILABLE -> context.getString(R.string.device_identifier_unavailable)
            null -> context.getString(R.string.device_identifier_loading)
        }
    }
    val currentTime by produceState(initialValue = "") {
        val formatter = SimpleDateFormat("h:mm a", Locale.forLanguageTag("es-PA"))
        while (true) {
            value = formatter
                .format(Date())
                .replace("AM", "a.m")
                .replace("PM", "p.m")
            delay(1_000)
        }
    }

    val launchApp: (LaunchableApp) -> Unit = { app ->
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(app.packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        if (launchIntent != null) context.startActivity(launchIntent)
    }

    Column(
        modifier = modifier
            .background(Color(0xFF37BBD8))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .clickable { showPinDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color(0xFF474747)
                )
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(13.dp),
                color = Color.Black
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⌂",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = context.getString(R.string.dashboard_institution),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = context.getString(R.string.dashboard_apps),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.width(94.dp),
                text = context.getString(R.string.dashboard_network),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
                color = Color(0xFFF0F0F0),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    visibleAppSlots.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEachIndexed { colIndex, app ->
                                val tileColor = if (rowIndex == 0 && colIndex == 0) {
                                    Color(0xFFF59A38)
                                } else {
                                    Color(0xFF4BB8E3)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .background(tileColor, RoundedCornerShape(14.dp))
                                        .clickable(enabled = app != null) { app?.let(launchApp) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        rowIndex == 0 && colIndex == 0 -> {
                                            Text(
                                                text = context.getString(R.string.dashboard_pda_label),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }

                                        rowIndex == 0 && colIndex == 1 -> {
                                            Icon(
                                                imageVector = Icons.Filled.CameraAlt,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }

                                        rowIndex == 1 && colIndex == 0 -> {
                                            Icon(
                                                imageVector = Icons.Filled.Image,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }

                                        else -> {
                                            Icon(
                                                imageVector = Icons.Filled.EditNote,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .width(94.dp)
                    .height(180.dp),
                color = Color(0xFFF0F0F0),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = currentTime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF333333)
                    )
                    DividerLine()
                    Icon(Icons.Filled.BatteryStd, contentDescription = null, tint = Color(0xFF202020))
                    DividerLine()
                    Icon(Icons.Filled.Wifi, contentDescription = null, tint = Color(0xFF202020))
                    DividerLine()
                    Icon(Icons.Filled.SignalCellular4Bar, contentDescription = null, tint = Color(0xFF202020))
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color(0xFFEFEFEF),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.MailOutline,
                    contentDescription = null,
                    tint = Color(0xFFC2C2C2)
                )
                Text(
                    text = context.getString(R.string.dashboard_no_messages),
                    color = Color(0xFFB7B7B7),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = imeiText,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = context.getString(R.string.dashboard_managed_by_it),
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Mantenemos controles de soporte solo cuando aun no esta en full-kiosk.
        if (!kioskManager.canUseFullKiosk()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!kioskManager.isAdminActive()) {
                    Button(onClick = onRequestAdmin, modifier = Modifier.fillMaxWidth()) {
                        Text(text = context.getString(R.string.enable_admin))
                    }
                }
                Button(onClick = onEnableDeviceOwner, modifier = Modifier.fillMaxWidth()) {
                    Text(text = context.getString(R.string.enable_device_owner))
                }
                Button(onClick = onRefreshStatus, modifier = Modifier.fillMaxWidth()) {
                    Text(text = context.getString(R.string.refresh_status))
                }
                TextButton(onClick = onUninstall, modifier = Modifier.fillMaxWidth()) {
                    Text(text = context.getString(R.string.uninstall_device_owner), color = Color.White)
                }
            }
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

@Composable
private fun DividerLine() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFD8D8D8))
    )
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
            onUninstall = {},
            onRefreshStatus = {},
            onEnableDeviceOwner = {}
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
