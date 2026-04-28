package com.example.kioskopda.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("pin") val pin: String,
    @SerializedName("imei") val imei: String
)

/** Respuesta única del backend — success=true o false */
data class LoginResponse(
    val success: Boolean,
    val titulo: String,
    val mensaje: String,
    // Solo presente cuando success=true
    val data: List<DeviceData>? = null,
    // Solo presentes cuando success=false
    val intentos: Int? = null,
    @SerializedName("max_intentos") val maxIntentos: Int? = null
)

data class DeviceData(
    val id: Int,
    val modelo: String,
    val numeracion: String,
    val imei: String,
    val estado: Int,
    val ubicado: String
)
