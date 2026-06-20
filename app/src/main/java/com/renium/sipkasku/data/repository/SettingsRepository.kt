package com.renium.sipkasku.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.core.content.edit

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("sipkasku_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "AUTO") ?: "AUTO")

    fun getThemeMode(): Flow<String> = _themeMode

    fun setThemeMode(value: String) {
        prefs.edit {putString("theme_mode", value)}
        _themeMode.value = value
    }

}
