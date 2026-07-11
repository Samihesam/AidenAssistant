package com.example.personalassistant

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat

class AlwaysListeningService : Service(), RecognitionListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private val wakeWord = "دستیار"

    companion object {
        const val CHANNEL_ID = "assistant_listening_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_WAKE_WORD_DETECTED = "com.example.personalassistant.WAKE_WORD_DETECTED"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("در حال گوش دادن..."))
        startListeningCycle()
        return START_STICKY
    }

    private fun startListeningCycle() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000)
        }
        speechRecognizer.startListening(recognizerIntent)
    }

    override fun onResults(results: android.os.Bundle?) {
        handleRecognizedText(results)
        startListeningCycle()
    }

    override fun onPartialResults(partialResults: android.os.Bundle?) {
        handleRecognizedText(partialResults)
    }

    private fun handleRecognizedText(bundle: android.os.Bundle?) {
        val matches = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim() ?: return

        if (text.contains(wakeWord, ignoreCase = true)) {
            onWakeWordDetected(text)
        }
    }

    private fun onWakeWordDetected(fullText: String) {
        val broadcast = Intent(ACTION_WAKE_WORD_DETECTED).apply {
            putExtra("recognized_text", fullText)
        }
        sendBroadcast(broadcast)
        updateNotification("دستیار فعال شد ✓")
    }

    override fun onError(error: Int) {
        startListeningCycle()
    }

    override fun onReadyForSpeech(params: android.os.Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: android.os.Bundle?) {}

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "دستیار همیشه فعال",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("دستیار شخصی")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
