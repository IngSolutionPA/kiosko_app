package com.example.kioskopda.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

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

                    if (unread > 0 && uiState !is NotificacionesUiState.Error) {
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
                    text = when (uiState) {
                        is NotificacionesUiState.Error -> "Sin conexión"
                        is NotificacionesUiState.Loading -> "Cargando mensajes"
                        is NotificacionesUiState.Success -> when {
                            totalCount <= 0 -> "No hay mensajes registrados"
                            unread > 0 -> "$unread sin leer de $totalCount mensajes"
                            else -> "$totalCount mensajes registrados"
                        }
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
                        val skeletonCount = when {
                            totalCount >= 8 -> 8
                            totalCount in 1..7 -> totalCount
                            else -> 6
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(skeletonCount) {
                                NotificacionSkeleton()
                            }
                        }
                    }

                    is NotificacionesUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.MailOutline,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E0),
                                modifier = Modifier.size(36.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Sin conexión",
                                color = Color(0xFFE53E3E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    is NotificacionesUiState.Success -> {
                        if (state.items.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
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
                            NotificacionesListContent(
                                items = state.items,
                                readIds = readIds,
                                onItemClick = { item ->
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

@Composable
private fun NotificacionesListContent(
    items: List<NotificacionItem>,
    readIds: Set<Int>,
    onItemClick: (NotificacionItem) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(items) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "messagesFade"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { item ->
            NotificacionCard(
                item = item,
                isRead = item.id in readIds,
                onClick = { onItemClick(item) }
            )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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

@Composable
fun NotificacionSkeleton() {
    val shimmerBrush = rememberShimmerBrush()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp)),
                brush = shimmerBrush
            )

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(13.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    brush = shimmerBrush
                )

                Spacer(Modifier.height(7.dp))

                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    brush = shimmerBrush
                )

                Spacer(Modifier.height(5.dp))

                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    brush = shimmerBrush
                )

                Spacer(Modifier.height(7.dp))

                ShimmerBox(
                    modifier = Modifier
                        .width(95.dp)
                        .height(9.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    brush = shimmerBrush
                )
            }
        }
    }
}

@Composable
private fun ShimmerBox(
    modifier: Modifier,
    brush: Brush
) {
    Box(
        modifier = modifier.background(brush)
    )
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")

    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF8FAFC),
            Color(0xFFE2E8F0)
        ),
        start = Offset(translateAnim - 1000f, translateAnim - 1000f),
        end = Offset(translateAnim, translateAnim)
    )
}