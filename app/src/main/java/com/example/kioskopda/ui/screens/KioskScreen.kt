package com.example.kioskopda.ui.screens

import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kioskopda.DeviceIdentifier
import com.example.kioskopda.DeviceIdentifierSource
import com.example.kioskopda.network.NotificacionItem
import com.example.kioskopda.ui.components.DividerLine
import com.example.kioskopda.R
import com.example.kioskopda.kiosk.KioskManager
import com.example.kioskopda.ui.theme.KioskoPDATheme
import com.example.kioskopda.ui.utils.KioskShortcutType
import com.example.kioskopda.ui.utils.openKioskShortcut
import com.example.kioskopda.ui.utils.rememberAppIcon
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
    // Navigation state
    var currentScreen by remember { mutableStateOf<KioskNavScreen>(KioskNavScreen.Main) }

    when (val screen = currentScreen) {
        is KioskNavScreen.Main -> KioskMainContent(
            modifier = modifier,
            deviceIdentifier = deviceIdentifier,
            kioskManager = kioskManager,
            onRequestAdmin = onRequestAdmin,
            onExitKiosk = onExitKiosk,
            onUninstall = onUninstall,
            onRefreshStatus = onRefreshStatus,
            onEnableDeviceOwner = onEnableDeviceOwner,
            onOpenNotifications = { currentScreen = KioskNavScreen.NotificationList },
            onOpenNotificationDetail = { item -> currentScreen = KioskNavScreen.NotificationDetail(item) }
        )
        is KioskNavScreen.NotificationList -> NotificacionesListScreen(
            onBack = { currentScreen = KioskNavScreen.Main },
            onOpenDetail = { item -> currentScreen = KioskNavScreen.NotificationDetail(item) }
        )
        is KioskNavScreen.NotificationDetail -> NotificacionDetailScreen(
            item = screen.item,
            onBack = { currentScreen = KioskNavScreen.NotificationList }
        )
    }
}

private sealed class KioskNavScreen {
    object Main : KioskNavScreen()
    object NotificationList : KioskNavScreen()
    data class NotificationDetail(val item: NotificacionItem) : KioskNavScreen()
}

@Composable
private fun KioskMainContent(
    modifier: Modifier = Modifier,
    deviceIdentifier: DeviceIdentifier?,
    kioskManager: KioskManager,
    onRequestAdmin: () -> Unit,
    onExitKiosk: () -> Unit,
    onUninstall: () -> Unit,
    onRefreshStatus: () -> Unit,
    onEnableDeviceOwner: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenNotificationDetail: (NotificacionItem) -> Unit
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    val exitPinViewModel: ExitPinViewModel = viewModel()
    val notificacionesViewModel: NotificacionesViewModel = viewModel()
    val notifState by notificacionesViewModel.uiState.collectAsState()
    val totalCount by notificacionesViewModel.totalCount.collectAsState()
    val readIds by notificacionesViewModel.readIds.collectAsState()

    LaunchedEffect(Unit) { notificacionesViewModel.loadPreview() }

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

    // IMEI puro para pasarlo al ViewModel
    val rawImei = remember(deviceIdentifier) { deviceIdentifier?.value ?: "" }
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
    }

    // ── Estado WiFi en tiempo real ──────────────────────────────────────
    val isWifiConnected by produceState(initialValue = false) {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        // Valor inicial
        value = cm.getNetworkCapabilities(cm.activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        // Escucha cambios
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { value = true }
            override fun onLost(network: Network) { value = false }
        }

        cm.registerNetworkCallback(request, callback)
        awaitDispose { cm.unregisterNetworkCallback(callback) }
    }

    val wifiColor = if (isWifiConnected) Color(0xFF4CAF50) else Color(0xFFBDBDBD)

    // ── Estado señal celular en tiempo real ─────────────────────────────
    val isCellularConnected by produceState(initialValue = false) {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        value = cm.getNetworkCapabilities(cm.activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { value = true }
            override fun onLost(network: Network) { value = false }
        }
        cm.registerNetworkCallback(request, callback)
        awaitDispose { cm.unregisterNetworkCallback(callback) }
    }
    val cellularColor = if (isCellularConnected) Color(0xFF4CAF50) else Color(0xFFBDBDBD)

    // ── Linterna ──────────────────────────────────────────────────────────
    var isTorchOn by remember { mutableStateOf(false) }

    fun toggleTorch() {
        try {
            val cm = context.getSystemService(android.content.Context.CAMERA_SERVICE)
                    as CameraManager
            // Buscamos cualquier cámara con flash disponible (sin filtrar por trasera
            // porque en algunos Honor la característica puede no reportarse como BACK)
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                cm.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) {
                val next = !isTorchOn
                cm.setTorchMode(cameraId, next)
                isTorchOn = next   // actualiza el ícono inmediatamente
            }
        } catch (_: Exception) { }
    }

    // Sincroniza el ícono si alguien apaga/enciende la linterna desde afuera
    DisposableEffect(Unit) {
        val cm = runCatching {
            context.getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager
        }.getOrNull()
        val cameraId = runCatching {
            cm?.cameraIdList?.firstOrNull { id ->
                cm.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()

        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cid: String, enabled: Boolean) {
                if (cid == cameraId) isTorchOn = enabled
            }
            override fun onTorchModeUnavailable(cid: String) {
                if (cid == cameraId) isTorchOn = false
            }
        }
        cm?.registerTorchCallback(callback, Handler(Looper.getMainLooper()))
        onDispose { cm?.unregisterTorchCallback(callback) }
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
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
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
                            color = Color(0xFFFFFFFF),
                            packageName = "com.imaapp.proyectoappcedula",
                            fallbackIcon = Icons.Filled.Apps,
                            label = context.getString(R.string.dashboard_pda_label),
                            onClick = { openKioskShortcut(context, KioskShortcutType.PDA) }
                        )
                        AppTile(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFFFFFFF),
                            packageName = "com.hihonor.camera",
                            fallbackIcon = Icons.Filled.PhotoCamera,
                            label = "Cámara",
                            onClick = { openKioskShortcut(context, KioskShortcutType.CAMERA) }
                        )
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
                            color = Color(0xFFFFFFFF),
                            packageName = "com.hihonor.photos",
                            fallbackIcon = Icons.Filled.Photo,
                            label = "Galería",
                            onClick = { openKioskShortcut(context, KioskShortcutType.GALLERY) }
                        )
                        AppTile(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFFFFFFF),
                            packageName = "com.hihonor.notepad",
                            label = "Notas",
                            fallbackIcon = Icons.AutoMirrored.Filled.Note,
                            onClick = { openKioskShortcut(context, KioskShortcutType.NOTES) }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .width(94.dp)
                    .wrapContentHeight(),
                color = Color(0xFFF0F0F0),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    // Batería
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.BatteryStd,
                            contentDescription = null,
                            tint = batteryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "$batteryLevel%", color = batteryColor, fontSize = 10.sp)
                    }
                    DividerLine()
                    // WiFi
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                Intent(Settings.Panel.ACTION_WIFI)
                            } else {
                                Intent(Settings.ACTION_WIFI_SETTINGS)
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                context.startActivity(
                                    Intent(Settings.ACTION_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Wifi,
                            contentDescription = "WiFi",
                            tint = wifiColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isWifiConnected) "WiFi" else "Sin WiFi",
                            fontSize = 9.sp,
                            color = wifiColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    DividerLine()
                    // Señal celular
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.SignalCellular4Bar,
                            contentDescription = null,
                            tint = cellularColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isCellularConnected) "Red" else "Sin red",
                            fontSize = 9.sp,
                            color = cellularColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    DividerLine()
                    // Brillo — abre ajustes de pantalla del sistema
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_DISPLAY_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BrightnessHigh,
                            contentDescription = "Brillo",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "Brillo", fontSize = 9.sp, color = Color(0xFF777777))
                    }
                    DividerLine()
                    // Linterna
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { toggleTorch() }
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Filled.FlashlightOn
                                          else Icons.Filled.FlashlightOff,
                            contentDescription = "Linterna",
                            tint = if (isTorchOn) Color(0xFF673AB7) else Color(0xFFBDBDBD),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isTorchOn) "ON" else "OFF",
                            fontSize = 9.sp,
                            color = if (isTorchOn) Color(0xFF673AB7) else Color(0xFF999999),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ── Mensajes (preview 3) ──────────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color(0xFFEFEFEF),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header row con total y badge de no leídos
                val unread = notificacionesViewModel.unreadCount
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (totalCount > 0) "Mensajes" else "Mensajes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                        if (unread > 0) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE53935), RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unread > 99) "99+" else unread.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Ver más →",
                        fontSize = 12.sp,
                        color = Color(0xFF37BBD8),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onOpenNotifications() }
                    )
                }

                when (val state = notifState) {
                    is NotificacionesUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = Color(0xFF37BBD8),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    is NotificacionesUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.MailOutline, contentDescription = null, tint = Color(0xFFC2C2C2))
                                Text(text = "Sin conexión", color = Color(0xFFB7B7B7), fontSize = 12.sp)
                            }
                        }
                    }
                    is NotificacionesUiState.Success -> {
                        if (state.items.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.MailOutline, contentDescription = null, tint = Color(0xFFC2C2C2))
                                    Text(
                                        text = context.getString(R.string.dashboard_no_messages),
                                        color = Color(0xFFB7B7B7),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.items.forEach { item ->
                                    NotifPreviewRow(
                                        item = item,
                                        isRead = item.id in readIds,
                                        onClick = {
                                            notificacionesViewModel.markAsRead(item.id)
                                            onOpenNotificationDetail(item)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
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
            },
            imei = rawImei,
            viewModel = exitPinViewModel
        )
    }
}

@Composable
private fun NotifPreviewRow(item: NotificacionItem, isRead: Boolean, onClick: () -> Unit) {
    val bgColor = if (isRead) Color.White else Color(0xFFE8F7FC)
    val titleWeight = if (isRead) FontWeight.Normal else FontWeight.Bold

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isRead) Color(0xFFBDBDBD) else Color(0xFF37BBD8),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MailOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (!isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFE53935), RoundedCornerShape(50))
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.titulo,
                fontWeight = titleWeight,
                fontSize = 13.sp,
                color = Color(0xFF222222),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.mensaje,
                fontSize = 11.sp,
                color = Color(0xFF777777),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF37BBD8), RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun AppTile(
    modifier: Modifier = Modifier,
    color: Color,
    packageName: String,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String? = null,
    onClick: () -> Unit
) {
    val appIcon = rememberAppIcon(packageName)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (appIcon != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .paint(
                            painter = BitmapPainter(appIcon),
                            contentScale = ContentScale.Fit
                        )
                )
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 👇 SIEMPRE muestra el nombre
            if (label != null) {
                Text(
                    text = label,
                    color = Color.Black, // 👈 aquí cambias color del texto
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
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

