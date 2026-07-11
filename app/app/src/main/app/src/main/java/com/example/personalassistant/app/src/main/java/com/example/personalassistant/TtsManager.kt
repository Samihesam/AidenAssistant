package com.example.personalassistant

import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object TtsManager {

    private const val API_KEY = "YOUR_API_KEY_HERE"
    private const val BASE_URL = "https://api.elevenlabs.io/v1/text-to-speech"

    private val client = OkHttpClient()

    val voices = listOf(
        VoiceProfile("زن ۱ - آرام", "VOICE_ID_FEMALE_1", Gender.FEMALE),
        VoiceProfile("زن ۲ - پرانرژی", "VOICE_ID_FEMALE_2", Gender.FEMALE),
        VoiceProfile("زن ۳ - رسمی", "VOICE_ID_FEMALE_3", Gender.FEMALE),
        VoiceProfile("زن ۴ - جوان", "VOICE_ID_FEMALE_4", Gender.FEMALE),
        VoiceProfile("زن ۵ - گرم", "VOICE_ID_FEMALE_5", Gender.FEMALE),
        VoiceProfile("مرد ۱ - آرام", "VOICE_ID_MALE_1", Gender.MALE),
        VoiceProfile("مرد ۲ - پرانرژی", "VOICE_ID_MALE_2", Gender.MALE),
        VoiceProfile("مرد ۳ - رسمی", "VOICE_ID_MALE_3", Gender.MALE),
        VoiceProfile("مرد ۴ - جوان", "VOICE_ID_MALE_4", Gender.MALE),
        VoiceProfile("مرد ۵ - عمیق", "VOICE_ID_MALE_5", Gender.MALE),
    )

    enum class Gender { MALE, FEMALE }
    data class VoiceProfile(val label: String, val voiceId: String, val gender: Gender)

    suspend fun speak(text: String, voice: VoiceProfile, cacheDir: File, onDone: () -> Unit = {}) {
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("text", text)
                    put("model_id", "eleven_multilingual_v2")
                    put("voice_settings", JSONObject().apply {
                        put("stability", 0.5)
                        put("similarity_boost", 0.75)
                    })
                }

                val request = Request.Builder()
                    .url("$BASE_URL/${voice.voiceId}")
                    .addHeader("xi-api-key", API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("TTS request failed: ${response.code}")
                }

                val audioFile = File(cacheDir, "tts_output.mp3")
                FileOutputStream(audioFile).use { out ->
                    response.body?.byteStream()?.copyTo(out)
                }

                withContext(Dispatchers.Main) {
                    val player = MediaPlayer()
                    player.setDataSource(audioFile.absolutePath)
                    player.prepare()
                    player.setOnCompletionListener {
                        it.release()
                        onDone()
                    }
                    player.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onDone() }
            }
        }
    }
}
