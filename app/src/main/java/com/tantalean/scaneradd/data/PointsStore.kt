package com.tantalean.scaneradd.data

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "points_store")

class PointsStore(private val context: Context) {

    private val KEY_POINTS = intPreferencesKey("points")

    val points: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_POINTS] ?: 0
    }

    suspend fun addPoints(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_POINTS] ?: 0
            prefs[KEY_POINTS] = current + amount
        }
    }

    suspend fun consumePoints(amount: Int): Boolean {
        var ok = false
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_POINTS] ?: 0
            if (current >= amount) {
                prefs[KEY_POINTS] = current - amount
                ok = true
            }
        }
        return ok
    }
}