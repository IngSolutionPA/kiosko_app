package com.example.kioskopda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kioskopda.network.NotificacionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesListScreen(
    viewModel: NotificacionesViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenDetail: (NotificacionItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val readIds by viewModel.readIds.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val unread = viewModel.unreadCount

    LaunchedEffect(Unit) { viewModel.loadAll() }

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
                    tint = Color(0xFF2D3748)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Mensajes",
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    if (unread > 0) {
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp)
                                .background(Color(0xFFE53E3E), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unread > 99) "99+" else unread.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when {
                        totalCount <= 0 -> "No hay mensajes registrados"
                        unread > 0 -> "$unread sin leer de $totalCount mensajes"
                        else -> "$totalCount mensajes registrados"
                    },
                    color = Color(0xFF718096),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshAll() }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 2.dp
            ) {
                when (val state = uiState) {
                    is NotificacionesUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF3182CE))
                        }
                    }

                    is NotificacionesUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.MailOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFCBD5E0),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "No se pudieron cargar los mensajes",
                                    color = Color(0xFFE53E3E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = state.message,
                                    color = Color(0xFF718096),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    is NotificacionesUiState.Success -> {
                        if (state.items.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.MailOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFCBD5E0),
                                        modifier = Modifier.size(38.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Sin mensajes",
                                        color = Color(0xFF718096),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Los mensajes aparecerán aquí.",
                                        color = Color(0xFFA0AEC0),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(state.items) { item ->
                                    NotificacionCard(
                                        item = item,
                                        isRead = item.id in readIds,
                                        onClick = {
                                            viewModel.markAsRead(item.id)
                                            onOpenDetail(item)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionCard(
    item: NotificacionItem,
    isRead: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (isRead) Color(0xFFF8FAFC) else Color(0xFFEBF8FF)
    val iconBg = if (isRead) Color(0xFFCBD5E0) else Color(0xFF3182CE)
    val titleWeight = if (isRead) FontWeight.SemiBold else FontWeight.Bold

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MailOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.titulo,
                        fontWeight = titleWeight,
                        fontSize = 14.sp,
                        color = Color(0xFF2D3748),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(6.dp))

                    PrioridadBadge(item.prioridad)
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    text = item.mensaje,
                    fontSize = 12.sp,
                    color = Color(0xFF718096),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.fecha.isNullOrBlank() || !item.hora.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = listOfNotNull(item.fecha, formatBackendTime(item.hora))
                            .joinToString(" · "),
                        fontSize = 10.sp,
                        color = Color(0xFFA0AEC0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!isRead) {
                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(Color(0xFF3182CE), CircleShape)
                )
            }
        }
    }
}
