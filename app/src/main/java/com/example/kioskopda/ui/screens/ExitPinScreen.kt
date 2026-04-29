package com.example.kioskopda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kioskopda.R
import com.example.kioskopda.ui.components.KioskAlertDialog

@Composable
fun ExitPinScreen(
    onCancel: () -> Unit,
    onPinOk: () -> Unit,
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
            // Bloquea que los toques pasen al composable que está debajo
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
                    .background(Color(0xFFEDF2F7))   // mismo fondo suave
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Quieres salir del modo kiosko?",
                        color = Color(0xFF2D3748),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF3182CE)   // azul corporativo
                        ) {
                            Box(
                                contentAlignment = Alignment.TopCenter,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Ingrese PIN de autorizacion",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(top = 18.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFFFFFFFF),
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

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
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.clickable {
                                        if (!isLoading) focusRequester.requestFocus()
                                    }
                                ) {
                                    repeat(4) { index ->
                                        val char = pin.getOrNull(index)?.toString() ?: ""
                                        val isActive = index == pin.length && pin.length < 4
                                        val isFilled = index < pin.length

                                        val borderColor = when {
                                            showError -> Color(0xFFE53E3E)
                                            isActive -> Color(0xFF3182CE)
                                            isFilled -> Color(0xFF3182CE)
                                            else -> Color(0xFFCBD5E0)
                                        }

                                        val backgroundColor = when {
                                            showError && isFilled -> Color(0xFFFFF5F5)
                                            isActive -> Color(0xFFEBF8FF)
                                            else -> Color(0xFFF7FAFC)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(backgroundColor, RoundedCornerShape(14.dp))
                                                .border(2.dp, borderColor, RoundedCornerShape(14.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char,
                                                color = Color(0xFF666666),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color(0xFFF7FAFC)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(
                                                1.dp,
                                                if (showError) Color(0xFFE53E3E) else Color(0xFFE2E8F0),
                                                RoundedCornerShape(18.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                color = Color(0xFF3182CE),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = if (showError) errorMensaje else "Ingrese el PIN",
                                                    color = if (showError) Color(0xFFE53E3E) else Color(0xFF718096),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )

                                                if (intentosText.isNotEmpty()) {
                                                    Text(
                                                        text = intentosText,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFFA0AEC0)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        if (!isLoading && pin.length == 4) {
                                            viewModel.validatePin(pin, imei)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    enabled = !isLoading && pin.length == 4,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3182CE)
                                    )
                                ) {
                                    Text("Autorizar", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.resetState()
                                        onCancel()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4A5568)
                                    )
                                ) {
                                    Text("Cancelar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Text(
                    text = context.getString(R.string.dashboard_managed_by_it),
                    color = Color(0xFF718096),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                )
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