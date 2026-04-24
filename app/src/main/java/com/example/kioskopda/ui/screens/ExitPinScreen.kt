package com.example.kioskopda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.kioskopda.kiosk.KioskConfig


@Composable
fun ExitPinScreen(
    onCancel: () -> Unit,
    onPinOk: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    // ============================
    // CONTENEDOR PRINCIPAL (pantalla completa)
    // ============================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF35BDD9))
    ) {

        // ============================
        // CONTENIDO CENTRADO (CARD)
        // ============================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ============================
            // TEXTO SUPERIOR
            // ============================
            Text(
                text = "¿Quieres salir del modo kiosko?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ============================
            // CONTENEDOR DE LAS CARDS
            // ============================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {

                // ============================
                // CARD NARANJA (HEADER)
                // ============================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFF9963B)
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

                // ============================
                // CARD BLANCA (CUERPO)
                // ============================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFF3F3F3)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.height(16.dp))

                        // ============================
                        // INPUT INVISIBLE (OTP real)
                        // ============================
                        BasicTextField(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pin = it
                                    showError = false
                                }
                            },
                            modifier = Modifier
                                .size(1.dp)
                                .alpha(0f)
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            singleLine = true
                        )

                        // ============================
                        // CAJITAS OTP
                        // ============================
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.clickable {
                                focusRequester.requestFocus()
                            }
                        ) {
                            repeat(4) { index ->

                                val char = pin.getOrNull(index)?.toString() ?: ""

                                val isActive = index == pin.length && pin.length < 4
                                val isFilled = index < pin.length

                                val borderColor = when {
                                    showError -> Color(0xFFFF5A5A)
                                    isActive -> Color(0xFFF9963B)
                                    isFilled -> Color(0xFFF9963B)
                                    else -> Color(0xFFD9D9D9)
                                }

                                val backgroundColor = when {
                                    showError && isFilled -> Color(0xFFFFF1F1)
                                    isActive -> Color(0xFFFFF0E3)
                                    else -> Color.White
                                }

                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(backgroundColor, RoundedCornerShape(14.dp))
                                        .border(
                                            2.dp,
                                            borderColor,
                                            RoundedCornerShape(14.dp)
                                        ),
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

                        // ============================
                        // CAJA DE ERROR
                        // ============================
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        1.dp,
                                        if (showError) Color(0xFFF9963B) else Color(0xFFE2E2E2),
                                        RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (showError) "Pin Incorrecto" else "Ingrese el PIN",
                                        color = if (showError) Color.Red else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (showError) "Quedan 2 intentos" else "",
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ============================
                        // BOTÓN AUTORIZAR
                        // ============================
                        Button(
                            onClick = {
                                if (pin == KioskConfig.exitPin) {
                                    onPinOk()
                                } else {
                                    showError = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF63CFE8)
                            )
                        ) {
                            Text("Autorizar", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ============================
                        // BOTÓN CANCELAR
                        // ============================
                        Button(
                            onClick = onCancel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D2D2D)
                            )
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ============================
        // TEXTO FIJO ABAJO
        // ============================
        Text(
            text = context.getString(R.string.dashboard_managed_by_it),
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}