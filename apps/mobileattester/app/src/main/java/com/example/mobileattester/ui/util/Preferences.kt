package com.example.mobileattester.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "Config"
)

class Preferences(private val context: Context) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("Config")
        private val enginesKey = stringSetPreferencesKey("addresses")

        private val defaultConfig = mutableSetOf("127.0.0.1:1883")



        private var _currentEngine : String? = null

        var currentEngine : String
            // getter
            get() = _currentEngine ?: defaultConfig.first()

            // setter
            set(value) {
                _currentEngine = value
            }
    }


    val engines: Flow<SortedSet<String>> by lazy {
        context.dataStore.data
        .map { preferences ->
            if(preferences[enginesKey] == null || preferences[enginesKey]!!.isEmpty())
                defaultConfig.toSortedSet()
            else
                preferences[enginesKey]!!.ifEmpty { defaultConfig }.also { if(_currentEngine == null) _currentEngine = it.first() }.toSortedSet()
        }
    }

    suspend fun saveEngines(engines : SortedSet<String>) {
        context.dataStore.edit { preferences ->
            preferences[enginesKey] = engines
        }
    }
}

