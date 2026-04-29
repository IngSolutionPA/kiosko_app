package com.example.kioskopda.network

import com.google.gson.annotations.SerializedName

data class NotificacionRequest(
    @SerializedName("imei") val imei: String
)

data class NotificacionResponse(
    val ok: Boolean,
    val titulo: String,
    val mensaje: String,
    val data: List<NotificacionData>? = null
)

data class NotificacionData(
    val id: Int,
    val imei: String,
    @SerializedName("numeracion_it")
    val numeracionIt: String,
    val ubicado: String,
    val estado: Int
)
