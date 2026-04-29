package com.example.kioskopda.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persiste en SharedPreferences el conjunto de IDs de notificaciones leídas
 * y el total de notificaciones obtenido del backend.
 */
class ReadStatusRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getReadIds(): Set<Int> =
        prefs.getStringSet(KEY_READ_IDS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet() ?: emptySet()

    fun markAsRead(id: Int) {
        val current = prefs.getStringSet(KEY_READ_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(id.toString())
        prefs.edit().putStringSet(KEY_READ_IDS, current).apply()
    }

    fun saveTotalCount(total: Int) {
        prefs.edit().putInt(KEY_TOTAL, total).apply()
    }

    fun getTotalCount(): Int = prefs.getInt(KEY_TOTAL, 0)

    companion object {
        private const val PREFS_NAME = "kiosco_notif_prefs"
        private const val KEY_READ_IDS = "read_ids"
        private const val KEY_TOTAL = "total_count"
    }
}

