package com.example.personalassistant

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class AndroidTtsManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    var availableVoices: List<VoiceProfile> = emptyList()
        private set

    data class VoiceProfile(val label: String, val voice: Voice, val gender: Gender)
    enum class Gender { MALE, FEMALE, UNKNOWN }

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                tts?.language = Locale("fa", "IR")
                buildVoiceList()
            }
        }
    }

    private fun buildVoiceList() {
        val engine = tts ?: return
        val allVoices = engine.voices ?: emptySet()

        val guessed = allVoices.mapNotNull { v ->
            val name = v.name.lowercase()
            val gender = when {
                name.contains("female") || name.contains("#f") -> Gender.FEMALE
                name.contains("male") || name.contains("#m") -> Gender.MALE
                else -> Gender.UNKNOWN
            }
            if (gender == Gender.UNKNOWN) null else VoiceProfile(v.name, v, gender)
        }

        val females = guessed.filter { it.gender == Gender.FEMALE }.take(5)
        val males = guessed.filter { it.gender == Gender.MALE }.take(5)

        val remaining = (allVoices - females.map { it.voice }.toSet() - males.map { it.voice }.toSet())
            .toList()
        val filledFemales = (females + remaining.take(5 - females.size).mapIndexed { i, v ->
            VoiceProfile("صدای زن ${females.size + i + 1}", v, Gender.FEMALE)
        }).take(5)
        val filledMales = (males + remaining.drop(5 - females.size).take(5 - males.size).mapIndexed { i, v ->
            VoiceProfile("صدای مرد ${males.size + i + 1}", v, Gender.MALE)
        }).take(5)

        availableVoices = filledFemales + filledMales
    }

    fun speak(text: String, voice: VoiceProfile? = null, onDone: () -> Unit = {}) {
        val engine = tts ?: return
        voice?.let { engine.voice = it.voice }

        val listener = object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { onDone() }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { onDone() }
        }
        engine.setOnUtteranceProgressListener(listener)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
