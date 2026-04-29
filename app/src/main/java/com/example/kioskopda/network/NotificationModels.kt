package com.example.kioskopda.network

import com.google.gson.annotations.SerializedName

data class NotificacionItem(
    val id: Int,
    val titulo: String,
    val mensaje: String,
    val prioridad: Int
)

data class NotificacionPagination(
    val page: Int,
    val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class NotificacionesResponse(
    val title: String,
    val message: String,
    @SerializedName("total_notificaciones") val totalNotificaciones: Int,
    val pagination: NotificacionPagination,
    val data: List<NotificacionItem>
)

