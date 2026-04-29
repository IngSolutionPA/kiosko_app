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
    object CheckingUnblock : PinUiState()
    object Success : PinUiState()
    data class Error(val mensaje: String, val intentosRestantes: Int) : PinUiState()
    data class NetworkError(val mensaje: String) : PinUiState()
    /** Dispositivo bloqueado por demasiados intentos fallidos */
    data class Blocked(val mensaje: String) : PinUiState()
}

class ExitPinViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PinUiState>(PinUiState.Idle)
    val uiState: StateFlow<PinUiState> = _uiState

    fun validatePin(pin: String, imei: String) {
        _uiState.value = PinUiState.Loading
        viewModelScope.launch {
            try {
                Log.d("KioskoPIN", "══════════════════════════════════════")
                Log.d("KioskoPIN", "📤 POST https://api.ima.gob.pa:30443/kiosco/login/")
                Log.d("KioskoPIN", "📤 PIN ingresado       : $pin")
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
                    Log.d("KioskoPIN", "✅ success=true | ${body.mensaje}")
                    body.data?.forEach { d ->
                        Log.d("KioskoPIN", "   📱 ${d.modelo} | ${d.numeracion} | IMEI=${d.imei} | ${d.ubicado}")
                    }
                    _uiState.value = PinUiState.Success
                } else {
                    val intentos = body.intentos ?: 0
                    val maxIntentos = body.maxIntentos ?: 0
                    val restantes = if (maxIntentos > 0) maxIntentos - intentos else -1
                    Log.d("KioskoPIN", "❌ success=false | ${body.mensaje} | intentos=$intentos/$maxIntentos | restantes=$restantes")

                    // Si agotó todos los intentos → pantalla de bloqueo
                    if (maxIntentos > 0 && intentos >= maxIntentos) {
                        Log.d("KioskoPIN", "🔒 DISPOSITIVO BLOQUEADO")
                        _uiState.value = PinUiState.Blocked(body.mensaje)
                    } else {
                        _uiState.value = PinUiState.Error(
                            mensaje = body.mensaje,
                            intentosRestantes = restantes
                        )
                    }
                }
                Log.d("KioskoPIN", "══════════════════════════════════════")
            } catch (e: Exception) {
                Log.e("KioskoPIN", "🔴 Excepción: ${e.javaClass.simpleName}: ${e.localizedMessage}")
                _uiState.value = PinUiState.NetworkError("Sin conexión: ${e.localizedMessage}")
            }
        }
    }

    /** Consulta al backend si el dispositivo fue desbloqueado por el administrador */
    fun checkUnblock(imei: String) {
        _uiState.value = PinUiState.CheckingUnblock
        viewModelScope.launch {
            try {
                Log.d("KioskoPIN", "🔄 Verificando desbloqueo para IMEI: $imei")
                // Enviamos pin vacío para que el backend solo valide el estado del dispositivo
                val response = RetrofitClient.api.login(pin = "", imei = imei)
                val body = response.body()
                Log.d("KioskoPIN", "🔄 Respuesta desbloqueo: ${Gson().toJson(body)}")

                if (body == null) {
                    _uiState.value = PinUiState.Blocked("Sin respuesta del servidor")
                    return@launch
                }

                val intentos = body.intentos ?: 0
                val maxIntentos = body.maxIntentos ?: 0

                if (maxIntentos > 0 && intentos >= maxIntentos) {
                    // Sigue bloqueado
                    Log.d("KioskoPIN", "🔒 Sigue bloqueado")
                    _uiState.value = PinUiState.Blocked(body.mensaje)
                } else {
                    // Ya fue desbloqueado → volver a ingresar PIN
                    Log.d("KioskoPIN", "🔓 Desbloqueado, volver a PIN")
                    _uiState.value = PinUiState.Idle
                }
            } catch (e: Exception) {
                Log.e("KioskoPIN", "🔴 Error verificando desbloqueo: ${e.localizedMessage}")
                _uiState.value = PinUiState.Blocked("Sin conexión: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _uiState.value = PinUiState.Idle
    }
}
