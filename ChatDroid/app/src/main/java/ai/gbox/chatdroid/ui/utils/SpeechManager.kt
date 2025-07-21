package ai.gbox.chatdroid.ui.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.*

class SpeechManager(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isListening = false
    private var isTtsReady = false
    
    private val _speechResults = Channel<SpeechResult>(Channel.UNLIMITED)
    val speechResults: Flow<SpeechResult> = _speechResults.receiveAsFlow()
    
    private val _ttsStatus = Channel<TtsStatus>(Channel.UNLIMITED)
    val ttsStatus: Flow<TtsStatus> = _ttsStatus.receiveAsFlow()
    
    sealed class SpeechResult {
        data class Success(val text: String) : SpeechResult()
        data class Error(val message: String) : SpeechResult()
        object Listening : SpeechResult()
        object Stopped : SpeechResult()
    }
    
    sealed class TtsStatus {
        object Ready : TtsStatus()
        object Speaking : TtsStatus()
        object Stopped : TtsStatus()
        data class Error(val message: String) : TtsStatus()
    }
    
    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }
    
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("SpeechManager", "Ready for speech")
                    _speechResults.trySend(SpeechResult.Listening)
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d("SpeechManager", "Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed - can be used for volume indicator
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // Partial audio buffer received
                }
                
                override fun onEndOfSpeech() {
                    Log.d("SpeechManager", "End of speech")
                    isListening = false
                    _speechResults.trySend(SpeechResult.Stopped)
                }
                
                override fun onError(error: Int) {
                    Log.e("SpeechManager", "Speech recognition error: $error")
                    isListening = false
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Unknown error"
                    }
                    _speechResults.trySend(SpeechResult.Error(errorMessage))
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        Log.d("SpeechManager", "Recognized text: $recognizedText")
                        _speechResults.trySend(SpeechResult.Success(recognizedText))
                    }
                    isListening = false
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial results - can be used for real-time display
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        Log.d("SpeechManager", "Partial result: ${matches[0]}")
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Additional events
                }
            })
        } else {
            Log.e("SpeechManager", "Speech recognition not available")
        }
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = textToSpeech?.setLanguage(Locale.getDefault())
                if (langResult == TextToSpeech.LANG_MISSING_DATA || 
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("SpeechManager", "Language not supported for TTS")
                    _ttsStatus.trySend(TtsStatus.Error("Language not supported"))
                } else {
                    Log.d("SpeechManager", "TTS initialized successfully")
                    isTtsReady = true
                    _ttsStatus.trySend(TtsStatus.Ready)
                }
            } else {
                Log.e("SpeechManager", "TTS initialization failed")
                _ttsStatus.trySend(TtsStatus.Error("TTS initialization failed"))
            }
        }
    }
    
    fun startListening(): Boolean {
        if (!hasRecordPermission()) {
            _speechResults.trySend(SpeechResult.Error("Recording permission not granted"))
            return false
        }
        
        if (isListening) {
            Log.w("SpeechManager", "Already listening")
            return false
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            Log.d("SpeechManager", "Started listening")
            return true
        } catch (e: Exception) {
            Log.e("SpeechManager", "Error starting speech recognition", e)
            _speechResults.trySend(SpeechResult.Error("Error starting speech recognition: ${e.message}"))
            return false
        }
    }
    
    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
            Log.d("SpeechManager", "Stopped listening")
        }
    }
    
    fun speak(text: String, interrupt: Boolean = true): Boolean {
        if (!isTtsReady) {
            Log.w("SpeechManager", "TTS not ready")
            _ttsStatus.trySend(TtsStatus.Error("TTS not ready"))
            return false
        }
        
        val queueMode = if (interrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        
        return try {
            val result = textToSpeech?.speak(text, queueMode, null, "utteranceId")
            if (result == TextToSpeech.SUCCESS) {
                Log.d("SpeechManager", "Speaking: $text")
                _ttsStatus.trySend(TtsStatus.Speaking)
                true
            } else {
                Log.e("SpeechManager", "TTS speak failed")
                _ttsStatus.trySend(TtsStatus.Error("Speech failed"))
                false
            }
        } catch (e: Exception) {
            Log.e("SpeechManager", "Error during TTS", e)
            _ttsStatus.trySend(TtsStatus.Error("Speech error: ${e.message}"))
            false
        }
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
        _ttsStatus.trySend(TtsStatus.Stopped)
        Log.d("SpeechManager", "Stopped speaking")
    }
    
    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate)
    }
    
    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }
    
    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun isListening(): Boolean = isListening
    
    fun isTtsReady(): Boolean = isTtsReady
    
    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
        isListening = false
        isTtsReady = false
        Log.d("SpeechManager", "Resources released")
    }
}