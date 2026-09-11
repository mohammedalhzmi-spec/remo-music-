package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceAction {
    PLAY, PAUSE, NEXT, PREVIOUS, SHUFFLE, REPEAT, OPEN_EQ, TOGGLE_LYRICS, MOOD_ENERGETIC, MOOD_CHILL, MOOD_WORKOUT, UNKNOWN
}

class VoiceCommandHandler(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _lastHeardText = MutableStateFlow("")
    val lastHeardText = _lastHeardText.asStateFlow()

    var onActionDetected: ((VoiceAction, String) -> Unit)? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("VoiceCommand", "Speech recognition not available on this device")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        Log.w("VoiceCommand", "Speech recognition error code: $error")
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _lastHeardText.value = text
                        processCommand(text)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بأمرك الموسيقي لـ REMOMUSIC...")
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            Log.e("VoiceCommand", "Failed to start speech recognition", e)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceCommand", "Error stopping speech recognition", e)
        }
        _isListening.value = false
    }

    fun processCommand(commandText: String) {
        val lower = commandText.lowercase().trim()
        val action = when {
            lower.contains("شغل") || lower.contains("تشغيل") || lower.contains("play") || lower.contains("تابع") || lower.contains("استئناف") -> VoiceAction.PLAY
            lower.contains("وقف") || lower.contains("إيقاف") || lower.contains("pause") || lower.contains("stop") || lower.contains("اسكت") -> VoiceAction.PAUSE
            lower.contains("التالي") || lower.contains("بعده") || lower.contains("next") || lower.contains("تخطي") || lower.contains("skip") -> VoiceAction.NEXT
            lower.contains("السابق") || lower.contains("قبله") || lower.contains("previous") || lower.contains("back") -> VoiceAction.PREVIOUS
            lower.contains("عشوائي") || lower.contains("خلط") || lower.contains("shuffle") -> VoiceAction.SHUFFLE
            lower.contains("كرر") || lower.contains("تكرار") || lower.contains("repeat") -> VoiceAction.REPEAT
            lower.contains("معادل") || lower.contains("صوت") || lower.contains("equalizer") || lower.contains("eq") -> VoiceAction.OPEN_EQ
            lower.contains("كلمات") || lower.contains("ترجمة") || lower.contains("lyrics") -> VoiceAction.TOGGLE_LYRICS
            lower.contains("حماسي") || lower.contains("نشاط") || lower.contains("energetic") -> VoiceAction.MOOD_ENERGETIC
            lower.contains("هادئ") || lower.contains("روقان") || lower.contains("chill") || lower.contains("استرخاء") -> VoiceAction.MOOD_CHILL
            lower.contains("تمرين") || lower.contains("جيم") || lower.contains("workout") -> VoiceAction.MOOD_WORKOUT
            else -> VoiceAction.UNKNOWN
        }
        onActionDetected?.invoke(action, commandText)
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
