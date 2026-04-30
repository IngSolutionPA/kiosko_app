package com.example.kioskopda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kioskopda.DeviceIdentifier
import com.example.kioskopda.DeviceIdentifierSource
import com.example.kioskopda.R
import com.example.kioskopda.ui.components.KioskAlertDialog

@Composable
fun ExitPinScreen(
    onCancel: () -> Unit,
    onPinOk: () -> Unit,
    deviceIdentifier: DeviceIdentifier?,
    imei: String = "",
    viewModel: ExitPinViewModel
) {
    val context = LocalContext.current

    var pin by remember { mutableStateOf("") }
    var alertTitle by remember { mutableStateOf<String?>(null) }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var alertSuccess by remember { mutableStateOf(false) }

    var isNotifying by remember { mutableStateOf(false) }
    var wasNotified by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

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

    LaunchedEffect(uiState) {
        when (uiState) {
            is PinUiState.Success -> {
                viewModel.resetState()
                onPinOk()
            }

            is PinUiState.Unlocked -> {
                viewModel.resetState()
                onCancel()
            }

            else -> {}
        }
    }

    val isLoading = uiState is PinUiState.Loading
    val showError = uiState is PinUiState.Error || uiState is PinUiState.NetworkError
    val isBlocked = uiState is PinUiState.Blocked
    val isCheckingUnblock = uiState is PinUiState.CheckingUnblock

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        if (isBlocked || isCheckingUnblock) {
            DeviceBlockedScreen(
                imei = imei,
                isChecking = isCheckingUnblock,
                isNotifying = isNotifying,
                wasNotified = wasNotified,
                onNotify = {
                    isNotifying = true

                    viewModel.notifyBlockedDevice(imei) { ok, title, message ->
                        wasNotified = ok
                        isNotifying = false

                        alertSuccess = ok
                        alertTitle = title ?: if (ok) "Éxito" else "Aviso"
                        alertMessage = message ?: if (ok) {
                            "Se ha notificado correctamente"
                        } else {
                            "No se pudo enviar la solicitud"
                        }
                    }
                },
                onCheckUnblock = {
                    viewModel.checkUnblock(imei) { title, message ->
                        if (message != null) {
                            alertSuccess = false
                            alertTitle = title ?: "Aviso"
                            alertMessage = message
                        }
                    }
                }
            )
        } else {
            val errorMensaje = when (val s = uiState) {
                is PinUiState.Error -> s.mensaje
                is PinUiState.NetworkError -> s.mensaje
                else -> ""
            }

            val intentosText = when (val s = uiState) {
                is PinUiState.Error -> {
                    if (s.intentosRestantes >= 0) {
                        "Quedan ${s.intentosRestantes} intentos"
                    } else {
                        ""
                    }
                }

                else -> ""
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F8FB))
                    .padding(horizontal = 22.dp, vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF2563EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Salida del modo kiosco",
                        color = Color(0xFF1F2937),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Ingrese el PIN autorizado para continuar",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(22.dp)
                                )

                                Text(
                                    text = "Ingrese PIN de autorización",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            BasicTextField(
                                value = pin,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                        pin = it
                                        if (showError) viewModel.resetState()
                                    }
                                },
                                modifier = Modifier
                                    .size(1.dp)
                                    .alpha(0f)
                                    .focusRequester(focusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword
                                ),
                                singleLine = true,
                                enabled = !isLoading
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.clickable {
                                    if (!isLoading) focusRequester.requestFocus()
                                }
                            ) {
                                repeat(4) { index ->
                                    val char = pin.getOrNull(index)?.toString() ?: ""
                                    val isActive = index == pin.length && pin.length < 4
                                    val isFilled = index < pin.length

                                    val borderColor = when {
                                        showError -> Color(0xFFDC2626)
                                        isActive -> Color(0xFF2563EB)
                                        isFilled -> Color(0xFF2563EB)
                                        else -> Color(0xFFCBD5E1)
                                    }

                                    val backgroundColor = when {
                                        showError && isFilled -> Color(0xFFFEF2F2)
                                        isActive -> Color(0xFFEFF6FF)
                                        isFilled -> Color(0xFFEFF6FF)
                                        else -> Color(0xFFF8FAFC)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .background(backgroundColor, RoundedCornerShape(16.dp))
                                            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char,
                                            color = Color(0xFF0F172A),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = if (showError) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            1.dp,
                                            if (showError) Color(0xFFDC2626) else Color(0xFFE2E8F0),
                                            RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF2563EB),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (showError) errorMensaje else "Ingrese los 4 dígitos del PIN",
                                                color = if (showError) Color(0xFFDC2626) else Color(0xFF64748B),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.Center
                                            )

                                            if (intentosText.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(3.dp))

                                                Text(
                                                    text = intentosText,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF94A3B8),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            Button(
                                onClick = {
                                    if (!isLoading && pin.length == 4) {
                                        viewModel.validatePin(pin, imei)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = RoundedCornerShape(18.dp),
                                enabled = !isLoading && pin.length == 4,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    disabledContainerColor = Color(0xFF93C5FD),
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Autorizar salida",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.resetState()
                                    onCancel()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                shape = RoundedCornerShape(18.dp),
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE2E8F0),
                                    contentColor = Color(0xFF334155),
                                    disabledContainerColor = Color(0xFFCBD5E1),
                                    disabledContentColor = Color(0xFF64748B)
                                )
                            ) {
                                Text(
                                    text = "Cancelar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ){
                    Text(
                        text = imeiText,
                        color = Color(0xFF4A5568),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = context.getString(R.string.dashboard_managed_by_it),
                        color = Color(0xFF718096),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (alertMessage != null) {
            KioskAlertDialog(
                title = alertTitle ?: "Aviso",
                message = alertMessage ?: "",
                isSuccess = alertSuccess,
                onDismiss = {
                    alertTitle = null
                    alertMessage = null
                    alertSuccess = false
                }
            )
        }
    }
}