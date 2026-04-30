package com.example.kioskopda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kioskopda.network.NotificacionItem
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars

// Helpers de prioridad reutilizables
fun prioridadLabel(p: Int) = when (p) { 1 -> "Alta"; 2 -> "Media"; else -> "Baja" }
fun prioridadBgColor(p: Int) = when (p) {
    1 -> Color(0xFFFDECEC)
    2 -> Color(0xFFFFF4E5)
    else -> Color(0xFFEAF7EE)
}

fun prioridadTextColor(p: Int) = when (p) {
    1 -> Color(0xFFB65C5C)
    2 -> Color(0xFFB7791F)
    else -> Color(0xFF4F8A65)
}

fun formatBackendTime(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return runCatching {
        val input = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("h:mm a", Locale.forLanguageTag("es-PA"))
        output.format(input.parse(raw)!!)
            .replace("AM", "a.m")
            .replace("PM", "p.m")
    }.getOrNull() ?: raw
}

@Composable
fun PrioridadBadge(prioridad: Int) {
    Box(
        modifier = Modifier
            .background(prioridadBgColor(prioridad), RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = prioridadLabel(prioridad),
            color = prioridadTextColor(prioridad),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun NotificacionDetailScreen(
    item: NotificacionItem,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FB))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .padding(top = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF2D3748),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = "Detalle del mensaje",
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = "Información recibida por el dispositivo",
                    color = Color(0xFF718096),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color.White,
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color(0xFFEAF2F8), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MailOutline,
                            contentDescription = null,
                            tint = Color(0xFF3182CE),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    PrioridadBadge(item.prioridad)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.titulo,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        color = Color(0xFF1F2937),
                        lineHeight = 26.sp
                    )

                    if (!item.fecha.isNullOrBlank() || !item.hora.isNullOrBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!item.fecha.isNullOrBlank()) {
                                InfoPill(text = item.fecha)
                            }

                            if (!item.hora.isNullOrBlank()) {
                                InfoPill(text = formatBackendTime(item.hora) ?: "")                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Mensaje",
                        color = Color(0xFF718096),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = item.mensaje,
                            fontSize = 15.sp,
                            color = Color(0xFF4A5568),
                            lineHeight = 23.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPill(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
