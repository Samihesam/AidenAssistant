package com.example.personalassistant

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    val supportedLanguages = listOf(
        LanguageOption("فارسی", "fa-IR"),
        LanguageOption("English", "en-US"),
        LanguageOption("العربية", "ar-SA"),
        LanguageOption("Türkçe", "tr-TR"),
        LanguageOption("Français", "fr-FR"),
        LanguageOption("Deutsch", "de-DE"),
        LanguageOption("Español", "es-ES"),
        LanguageOption("Русский", "ru-RU"),
        LanguageOption("中文", "zh-CN"),
        LanguageOption("हिन्दी", "hi-IN"),
        LanguageOption("اردو", "ur-PK")
    )

    data class LanguageOption(val label: String, val bcp47Code: String)

    private const val PREFS_NAME = "assistant_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "fa-IR") ?: "fa-IR"
    }

    fun saveLanguage(context: Context, bcp47Code: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, bcp47Code).apply()
    }

    fun applyUiLocale(context: Context, bcp47Code: String): Context {
        val locale = Locale.forLanguageTag(bcp47Code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun detectLanguageFromText(text: String): String {
        return when {
            text.any { it.code in 0x0600..0x06FF } -> "fa-IR"
            text.any { it.code in 0x4E00..0x9FFF } -> "zh-CN"
            text.any { it.code in 0x0400..0x04FF } -> "ru-RU"
            text.any { it.code in 0x0900..0x097F } -> "hi-IN"
            else -> "en-US"
        }
    }
}
