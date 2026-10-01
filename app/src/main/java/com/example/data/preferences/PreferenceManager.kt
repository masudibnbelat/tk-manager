package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_user_prefs", Context.MODE_PRIVATE)

    private val _accountName = MutableStateFlow(prefs.getString(KEY_ACCOUNT_NAME, "My Wallet") ?: "My Wallet")
    val accountName: StateFlow<String> = _accountName.asStateFlow()

    private val _currencyCode = MutableStateFlow(prefs.getString(KEY_CURRENCY_CODE, "BDT") ?: "BDT")
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY_SYMBOL, "৳") ?: "৳")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _avatar = MutableStateFlow(prefs.getString(KEY_AVATAR, "💼") ?: "💼")
    val avatar: StateFlow<String> = _avatar.asStateFlow()

    private val _hasCompletedSetup = MutableStateFlow(prefs.getBoolean(KEY_SETUP_COMPLETED, false))
    val hasCompletedSetup: StateFlow<Boolean> = _hasCompletedSetup.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "bn") ?: "bn")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun completeOnboarding(name: String, code: String, symbol: String, avatar: String) {
        prefs.edit()
            .putString(KEY_ACCOUNT_NAME, name)
            .putString(KEY_CURRENCY_CODE, code)
            .putString(KEY_CURRENCY_SYMBOL, symbol)
            .putString(KEY_AVATAR, avatar)
            .putBoolean(KEY_SETUP_COMPLETED, true)
            .apply()

        _accountName.value = name
        _currencyCode.value = code
        _currencySymbol.value = symbol
        _avatar.value = avatar
        _hasCompletedSetup.value = true
    }

    fun updateAccountName(name: String) {
        prefs.edit().putString(KEY_ACCOUNT_NAME, name).apply()
        _accountName.value = name
    }

    fun updateCurrency(code: String, symbol: String) {
        prefs.edit().putString(KEY_CURRENCY_CODE, code).putString(KEY_CURRENCY_SYMBOL, symbol).apply()
        _currencyCode.value = code
        _currencySymbol.value = symbol
    }

    fun updateAvatar(avatar: String) {
        prefs.edit().putString(KEY_AVATAR, avatar).apply()
        _avatar.value = avatar
    }

    fun updateLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _appLanguage.value = lang
    }

    fun updateThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _accountName.value = "My Wallet"
        _currencyCode.value = "BDT"
        _currencySymbol.value = "৳"
        _avatar.value = "💼"
        _hasCompletedSetup.value = false
        _appLanguage.value = "bn"
        _themeMode.value = "SYSTEM"
    }

    companion object {
        private const val KEY_ACCOUNT_NAME = "account_name"
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_AVATAR = "avatar_sticker"
        private const val KEY_SETUP_COMPLETED = "has_completed_setup"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
