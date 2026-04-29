package com.example.kioskopda.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kioskopda.data.ReadStatusRepository
import com.example.kioskopda.network.NotificacionItem
import com.example.kioskopda.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class NotificacionesUiState {
    object Loading : NotificacionesUiState()
    data class Success(val items: List<NotificacionItem>) : NotificacionesUiState()
    data class Error(val message: String) : NotificacionesUiState()
}

class NotificacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ReadStatusRepository(application)

    private val _uiState = MutableStateFlow<NotificacionesUiState>(NotificacionesUiState.Loading)
    val uiState: StateFlow<NotificacionesUiState> = _uiState

    private val _readIds = MutableStateFlow<Set<Int>>(repo.getReadIds())
    val readIds: StateFlow<Set<Int>> = _readIds

    private val _totalCount = MutableStateFlow(repo.getTotalCount())
    val totalCount: StateFlow<Int> = _totalCount

    /** IDs no leídos = total - leídos (mínimo 0) */
    val unreadCount: Int
        get() = maxOf(0, _totalCount.value - _readIds.value.size)

    fun markAsRead(id: Int) {
        repo.markAsRead(id)
        _readIds.value = repo.getReadIds()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = NotificacionesUiState.Loading
            try {
                val response = RetrofitClient.api.getNotificaciones(page = 1, limit = 100)
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.totalNotificaciones?.let { total ->
                        repo.saveTotalCount(total)
                        _totalCount.value = total
                    }
                    _uiState.value = NotificacionesUiState.Success(body?.data ?: emptyList())
                } else {
                    _uiState.value = NotificacionesUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = NotificacionesUiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    fun loadPreview() {
        viewModelScope.launch {
            _uiState.value = NotificacionesUiState.Loading
            try {
                val response = RetrofitClient.api.getNotificaciones(page = 1, limit = 3)
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.totalNotificaciones?.let { total ->
                        repo.saveTotalCount(total)
                        _totalCount.value = total
                    }
                    _uiState.value = NotificacionesUiState.Success(body?.data ?: emptyList())
                } else {
                    _uiState.value = NotificacionesUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = NotificacionesUiState.Error(e.message ?: "Error de conexión")
            }
        }
    }
}
