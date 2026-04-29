package com.example.kioskopda.network

import com.google.gson.annotations.SerializedName

data class CorreoRequest(
    @SerializedName("imei") val imei: String
)

data class CorreoResponse(
    val ok: Boolean,
    val titulo: String,
    val mensaje: String,
    val data: List<CorreoData>? = null
)

data class CorreoData(
    val id: Int,
    val imei: String,
    @SerializedName("numeracion_it")
    val numeracionIt: String,
    val ubicado: String,
    val estado: Int
)
