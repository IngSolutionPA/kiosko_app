package com.example.kioskopda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
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

@Composable
fun NotificacionesListScreen(
    viewModel: NotificacionesViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenDetail: (NotificacionItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val readIds by viewModel.readIds.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val unread = viewModel.unreadCount

    LaunchedEffect(Unit) { viewModel.loadAll() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF37BBD8))
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
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF474747)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (totalCount > 0) "Mensajes" else "Mensajes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
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
            when (val state = uiState) {
                is NotificacionesUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF37BBD8))
                    }
                }
                is NotificacionesUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
                is NotificacionesUiState.Success -> {
                    if (state.items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.MailOutline, contentDescription = null, tint = Color(0xFFC2C2C2))
                                Text(text = "Sin mensajes", color = Color(0xFFB7B7B7))
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

@Composable
fun NotificacionCard(item: NotificacionItem, isRead: Boolean = false, onClick: () -> Unit) {
    val bgColor = if (isRead) Color.White else Color(0xFFE8F7FC)
    val titleWeight = if (isRead) FontWeight.Normal else FontWeight.Bold

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isRead) Color(0xFFBDBDBD) else Color(0xFF37BBD8),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MailOutline, contentDescription = null, tint = Color.White)
                }
                if (!isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFE53935), RoundedCornerShape(50))
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.titulo,
                    fontWeight = titleWeight,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.mensaje,
                    fontSize = 12.sp,
                    color = Color(0xFF777777),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!isRead) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF37BBD8), RoundedCornerShape(50))
                )
            }
        }
    }
}
