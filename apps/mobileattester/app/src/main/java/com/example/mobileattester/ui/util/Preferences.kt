package com.example.mobileattester.ui.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Config")

object Preferences {
    var engines = mutableListOf("127.0.0.1:1883",
        "192.168.0.3:8036",
        "182.342.12.102:6760",
        "192.168.0.1:2000")
    var currentEngine: String = "127.0.0.1:1883"
    fun getEngines(context: Context, onComplete: (result: List<String>?) -> Unit = {}) {
        val enginesKey = stringSetPreferencesKey("addresses")
        val flow = context.dataStore.data.map { preferences ->
            // Completed value, defaulting to null if not set:
            val result = (preferences[enginesKey])?.toList() ?: null
            onComplete(result)
        }
    }

    suspend fun setEngines(context: Context, addresses: Set<String>) {
        val enginesKey = stringSetPreferencesKey("addresses")
        context.dataStore.edit { preferences ->
            preferences[enginesKey] = addresses
        }
    }
}