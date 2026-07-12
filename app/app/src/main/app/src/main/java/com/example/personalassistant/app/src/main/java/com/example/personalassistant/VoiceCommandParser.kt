package com.example.personalassistant

object VoiceCommandParser {

    sealed class AssistantCommand {
        data class ChangeVoice(val voiceIndex: Int?, val gender: AndroidTtsManager.Gender?) : AssistantCommand()
        data class ChangeAvatarVisibility(val show: Boolean) : AssistantCommand()
        data class ChangeLanguage(val languageLabel: String) : AssistantCommand()
        object None : AssistantCommand()
    }

    private val hidePhrases = listOf("برو کنار", "پنهان شو", "قایم شو", "دیده نشو")
    private val showPhrases = listOf("بیا جلو", "نشون بده خودتو", "ظاهر شو")

    fun parse(text: String): AssistantCommand {
        val normalized = text.trim()

        if (hidePhrases.any { normalized.contains(it) }) {
            return AssistantCommand.ChangeAvatarVisibility(show = false)
        }
        if (showPhrases.any { normalized.contains(it) }) {
            return AssistantCommand.ChangeAvatarVisibility(show = true)
        }

        if (normalized.contains("صدا") && (normalized.contains("عوض") || normalized.contains("تغییر"))) {
            val gender = when {
                normalized.contains("زن") -> AndroidTtsManager.Gender.FEMALE
                normalized.contains("مرد") -> AndroidTtsManager.Gender.MALE
                else -> null
            }
            val number = extractPersianOrArabicNumber(normalized)
            return AssistantCommand.ChangeVoice(voiceIndex = number, gender = gender)
        }

        LanguageManager.supportedLanguages.forEach { lang ->
            if (normalized.contains(lang.label, ignoreCase = true) &&
                (normalized.contains("زبان") || normalized.contains("صحبت کن"))
            ) {
                return AssistantCommand.ChangeLanguage(lang.label)
            }
        }

        return AssistantCommand.None
    }

    private fun extractPersianOrArabicNumber(text: String): Int? {
        val digitRegex = Regex("[0-9۰-۹]+")
        val match = digitRegex.find(text)?.value
        if (match != null) {
            return match.map { ch ->
                if (ch.code in 0x06F0..0x06F9) ch.code - 0x06F0 else ch - '0'
            }.fold(0) { acc, d -> acc * 10 + d }
        }
        val wordNumbers = mapOf(
            "یک" to 1, "دو" to 2, "سه" to 3, "چهار" to 4, "پنج" to 5
        )
        wordNumbers.forEach { (word, num) -> if (text.contains(word)) return num }
        return null
    }
}
