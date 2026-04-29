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
            .background(Color(0xFFEDF2F7))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF4A5568)
                )
            }
            Text(
                text = "Detalle del mensaje",
                color = Color(0xFF2D3748),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = Color(0xFFFFFFFF),
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono + badge prioridad en misma fila
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF3182CE), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MailOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    PrioridadBadge(item.prioridad)
                }

                Text(
                    text = item.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2D3748)
                )

                // Fecha y hora si están disponibles
                if (!item.fecha.isNullOrBlank() || !item.hora.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!item.fecha.isNullOrBlank()) {
                            Text(
                                text = "📅 ${item.fecha}",
                                fontSize = 12.sp,
                                color = Color(0xFF718096)
                            )
                        }
                        if (!item.hora.isNullOrBlank()) {
                            Text(
                                text = "🕐 ${formatBackendTime(item.hora)}",
                                fontSize = 12.sp,
                                color = Color(0xFF718096)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Text(
                    text = item.mensaje,
                    fontSize = 15.sp,
                    color = Color(0xFF4A5568),
                    lineHeight = 22.sp
                )
            }
        }
    }
}
