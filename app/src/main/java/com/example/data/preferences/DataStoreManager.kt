package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.Currencies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tk_manager_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_ACCOUNT_NAME = stringPreferencesKey("account_name")
        val KEY_CURRENCY_CODE = stringPreferencesKey("currency_code")
        val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val KEY_AVATAR_STICKER = stringPreferencesKey("avatar_sticker")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_TUTORIAL_SEEN = booleanPreferencesKey("tutorial_seen")

        val AVAILABLE_AVATARS = listOf(
            "💼", "💰", "💳", "🏦", "💎", "🚀", "🌟", "🎯", "👤", "🪙", "📊", "👑",
            "🦁", "🦊", "🐼", "😎", "🔥", "✨"
        )
    }

    val hasCompletedSetup: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val name = preferences[KEY_ACCOUNT_NAME]
        val currency = preferences[KEY_CURRENCY_CODE]
        !name.isNullOrBlank() && !currency.isNullOrBlank()
    }

    val accountName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACCOUNT_NAME] ?: "My Account"
    }

    val currencyCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_CURRENCY_CODE] ?: Currencies.DEFAULT.code
    }

    val currencySymbol: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_CURRENCY_SYMBOL] ?: Currencies.DEFAULT.symbol
    }

    val avatarSticker: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_AVATAR_STICKER] ?: "💼"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val hasSeenTutorial: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_TUTORIAL_SEEN] ?: false
    }

    suspend fun saveAccountName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCOUNT_NAME] = name.trim().ifEmpty { "My Account" }
        }
    }

    suspend fun saveCurrency(code: String, symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENCY_CODE] = code
            preferences[KEY_CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun saveAvatarSticker(sticker: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AVATAR_STICKER] = sticker
        }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setTutorialSeen(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TUTORIAL_SEEN] = seen
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
