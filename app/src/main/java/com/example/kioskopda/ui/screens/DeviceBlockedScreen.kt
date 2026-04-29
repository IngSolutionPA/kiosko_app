package com.example.kioskopda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kioskopda.R

@Composable
fun DeviceBlockedScreen(
    imei: String,
    isChecking: Boolean,
    isNotifying: Boolean,
    wasNotified: Boolean,
    onNotify: () -> Unit,
    onCheckUnblock: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDF2F7))
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
        ) {
            // CARD HEADER azul corporativo
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF3182CE)
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Dispositivo Bloqueado",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(top = 18.dp)
                    )
                }
            }

            // CARD CUERPO blanco con sombra sutil
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
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Spacer(modifier = Modifier.height(8.dp))

                    // ÍCONO DE CANDADO
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Color(0xFF3182CE),
                        modifier = Modifier.size(72.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // MENSAJE PRINCIPAL
                    Text(
                        text = "contacte a informatica",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF2D3748),
                        textAlign = TextAlign.Center
                    )

                    // IMEI
                    if (imei.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF7FAFC)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "IMEI: $imei",
                                    fontSize = 13.sp,
                                    color = Color(0xFF718096),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // BOTÓN VERIFICAR DESBLOQUEO
                    Button(
                        onClick = {
                            if (wasNotified) {
                                onCheckUnblock()
                            } else {
                                onNotify()
                            }
                        },
                        enabled = !isChecking && !isNotifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (wasNotified) Color(0xFF4A5568) else Color(0xFF3182CE),
                            disabledContainerColor = if (wasNotified) Color(0xFF718096) else Color(0xFF90CDF4)
                        )
                    ) {
                        if (isChecking || isNotifying) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (wasNotified) "Verificar desbloqueo" else "Notificar",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // TEXTO FIJO ABAJO
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
