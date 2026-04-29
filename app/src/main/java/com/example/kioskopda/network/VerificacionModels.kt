package com.example.kioskopda.network

import com.google.gson.annotations.SerializedName

data class ValidacionRequest(
    @SerializedName("imei") val imei: String
)

data class ValidacionResponse(
    val ok: Boolean,
    val title: String,
    val message: String,
    val blocked: Boolean,
)