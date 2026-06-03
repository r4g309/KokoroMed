package dev.r4g309.kokoromed.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("kokoro_settings")

enum class AppTheme { light, dark, system }

data class AppSettings(
    val theme: AppTheme = AppTheme.system,
    val timer: Boolean  = false,
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    private val KEY_THEME = stringPreferencesKey("theme")
    private val KEY_TIMER = booleanPreferencesKey("timer")

    val settings: Flow<AppSettings> = ctx.dataStore.data.map { prefs ->
        AppSettings(
            theme = prefs[KEY_THEME]
                ?.let { runCatching { AppTheme.valueOf(it) }.getOrDefault(AppTheme.system) }
                ?: AppTheme.system,
            timer = prefs[KEY_TIMER] ?: false,
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        ctx.dataStore.edit { it[KEY_THEME] = theme.name }
    }

    suspend fun setTimer(enabled: Boolean) {
        ctx.dataStore.edit { it[KEY_TIMER] = enabled }
    }
}
