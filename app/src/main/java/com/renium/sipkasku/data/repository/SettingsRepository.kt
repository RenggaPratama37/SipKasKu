package com.renium.sipkasku.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SettingsRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("sipkasku_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "AUTO") ?: "AUTO")
    private val _pocketMandatory = MutableStateFlow(prefs.getBoolean("pocket_mandatory", false))

    fun getThemeMode(): Flow<String> = _themeMode

    suspend fun setThemeMode(value: String) {
        prefs.edit().putString("theme_mode", value).apply()
        _themeMode.value = value
    }

    fun isPocketMandatory(): Flow<Boolean> = _pocketMandatory

    suspend fun setPocketMandatory(value: Boolean) {
        prefs.edit().putBoolean("pocket_mandatory", value).apply()
        _pocketMandatory.value = value
    }
}
