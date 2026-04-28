package com.example.kioskopda.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kioskopda.network.LoginRequest
import com.example.kioskopda.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PinUiState {
    object Idle : PinUiState()
    object Loading : PinUiState()
    object Success : PinUiState()
    data class Error(val mensaje: String, val intentosRestantes: Int) : PinUiState()
    data class NetworkError(val mensaje: String) : PinUiState()
}

class ExitPinViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PinUiState>(PinUiState.Idle)
    val uiState: StateFlow<PinUiState> = _uiState

    fun validatePin(pin: String, imei: String) {
        _uiState.value = PinUiState.Loading
        viewModelScope.launch {
            try {
                val request = LoginRequest(pin = pin, imei = imei)
                Log.d("KioskoPIN", "══════════════════════════════════════")
                Log.d("KioskoPIN", "📤 POST https://api.ima.gob.pa:30443/kiosco/login/")
                Log.d("KioskoPIN", "📤 PIN ingresado    : $pin")
                Log.d("KioskoPIN", "📤 IMEI del dispositivo: $imei")
                Log.d("KioskoPIN", "📤 Formato: application/x-www-form-urlencoded  pin=$pin&imei=$imei")

                val response = RetrofitClient.api.login(pin = pin, imei = imei)

                Log.d("KioskoPIN", "📥 HTTP Status: ${response.code()} ${response.message()}")

                val body = response.body()
                Log.d("KioskoPIN", "📥 Body completo: ${Gson().toJson(body)}")

                if (body == null) {
                    Log.e("KioskoPIN", "🔴 Body nulo — respuesta inesperada")
                    _uiState.value = PinUiState.NetworkError("Respuesta vacía del servidor")
                    return@launch
                }

                if (body.success) {
                    Log.d("KioskoPIN", "✅ success=true | titulo=${body.titulo} | mensaje=${body.mensaje}")
                    body.data?.forEach { d ->
                        Log.d("KioskoPIN", "   📱 ${d.modelo} | ${d.numeracion} | IMEI=${d.imei} | ${d.ubicado}")
                    }
                    _uiState.value = PinUiState.Success
                } else {
                    val intentos = body.intentos ?: 0
                    val maxIntentos = body.maxIntentos ?: 0
                    val restantes = if (maxIntentos > 0) maxIntentos - intentos else -1
                    Log.d("KioskoPIN", "❌ success=false | titulo=${body.titulo} | mensaje=${body.mensaje} | intentos=$intentos/$maxIntentos | restantes=$restantes")
                    _uiState.value = PinUiState.Error(
                        mensaje = body.mensaje,
                        intentosRestantes = restantes
                    )
                }
                Log.d("KioskoPIN", "══════════════════════════════════════")
            } catch (e: Exception) {
                Log.e("KioskoPIN", "🔴 Excepción: ${e.javaClass.simpleName}: ${e.localizedMessage}")
                _uiState.value = PinUiState.NetworkError("Sin conexión: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _uiState.value = PinUiState.Idle
    }
}

