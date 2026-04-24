package com.example.kioskopda.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kioskopda.DeviceIdentifier
import com.example.kioskopda.DeviceIdentifierSource
import com.example.kioskopda.ui.components.DividerLine
import com.example.kioskopda.R
import com.example.kioskopda.kiosk.KioskManager
import com.example.kioskopda.ui.theme.KioskoPDATheme
import com.example.kioskopda.ui.utils.KioskShortcutType
import com.example.kioskopda.ui.utils.openKioskShortcut
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var showPinDialog by remember { mutableStateOf(false) }

    val imeiText = remember(deviceIdentifier) {
        val value = deviceIdentifier?.value ?: ""

        when (deviceIdentifier?.source) {
            DeviceIdentifierSource.IMEI ->
                context.getString(R.string.dashboard_imei_format, value)

            DeviceIdentifierSource.ANDROID_ID ->
                context.getString(R.string.device_identifier_fallback_android_id, value)

            DeviceIdentifierSource.UNAVAILABLE ->
                context.getString(R.string.device_identifier_unavailable)

            else ->
                context.getString(R.string.device_identifier_loading)
        }
    }
    val currentTime by produceState(initialValue = "") {
        val formatter = SimpleDateFormat("h:mm a", Locale.forLanguageTag("es-PA"))
        while (true) {
            val now = Date()
            value = formatter.format(now)
                .replace("AM", "a.m")
                .replace("PM", "p.m")
            delay(1000)
        }
    }

    val batteryLevel by produceState(initialValue = 0) {
        val intent = context.registerReceiver(
            null,
            android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1

        value = if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            0
        }
    }

    val batteryColor = when {
        batteryLevel >= 60 -> Color(0xFF4CAF50) // verde
        batteryLevel >= 30 -> Color(0xFFFFA000) // naranja
        else -> Color(0xFFE53935) // rojo
    } //No es obligatorio, se quita si no les gusta.


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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // FILA 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        AppTile(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFF59A38),
                            onClick = {
                                openKioskShortcut(context, KioskShortcutType.PDA)
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.dashboard_pda_label),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        AppTile(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF4BB8E3),
                            onClick = {
                                openKioskShortcut(context, KioskShortcutType.CAMERA)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    // FILA 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        AppTile(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF4BB8E3),
                            onClick = {
                                openKioskShortcut(context, KioskShortcutType.GALLERY)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        AppTile(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF4BB8E3),
                            onClick = {
                                openKioskShortcut(context, KioskShortcutType.NOTES)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = null,
                                tint = Color.White
                            )
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
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center
                    )
                    DividerLine()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.BatteryStd,
                            contentDescription = null,
                            tint = batteryColor
                        )

                        Text(
                            text = "$batteryLevel%",
                            color = batteryColor
                        )
                    }
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

    val closeDialog = { showPinDialog = false }

    if (showPinDialog) {
        ExitPinScreen(
            onCancel = closeDialog,
            onPinOk = {
                closeDialog()
                onExitKiosk()
            }
        )
    }
}

@Composable
private fun AppTile(
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
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
            onUninstall = {},
            onRefreshStatus = {},
            onEnableDeviceOwner = {}
        )
    }
}

