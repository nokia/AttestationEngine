package com.example.mobileattester.ui.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mobileattester.ui.viewmodel.AttestationViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Config")

class Preferences(
    private val context: Context,
    private val viewModel: AttestationViewModel? = null,
) {

    // to make sure there's only one instance
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("Config")
        private val enginesKey = stringSetPreferencesKey("addresses")

        val defaultConfig = mutableSetOf("172.30.87.192/8520")
    }

    val engines: Flow<SortedSet<String>> by lazy {
        context.dataStore.data.map { preferences ->
            if (preferences[enginesKey] == null || preferences[enginesKey]!!.isEmpty()) defaultConfig.toSortedSet()
            else {
                preferences[enginesKey]!!.ifEmpty { defaultConfig }.also {
                    Log.e("isDefaultBaseUrl", viewModel?.isDefaultBaseUrl().toString())
                    if (viewModel?.isDefaultBaseUrl() == true) {
                        Log.e("Implicit BaseUrl", it.first().toString())
                        viewModel.baseUrl.value = "http://${it.first()}/"
                    }
                }.toSortedSet()
            }
        }
    }

    suspend fun saveEngines(engines: SortedSet<String>) {
        context.dataStore.edit { preferences ->
            preferences[enginesKey] = engines
        }
    }
}

