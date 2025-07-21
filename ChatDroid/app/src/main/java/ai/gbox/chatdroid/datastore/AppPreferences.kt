package ai.gbox.chatdroid.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AppPreferences {
    private const val DATASTORE_NAME = "app_preferences"
    
    private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)
    
    private lateinit var appContext: Context
    
    fun init(context: Context) {
        appContext = context.applicationContext
    }
    
    // Preference keys
    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_FONT_SIZE = floatPreferencesKey("font_size")
    private val KEY_SEND_ON_ENTER = booleanPreferencesKey("send_on_enter")
    private val KEY_VOICE_INPUT_ENABLED = booleanPreferencesKey("voice_input_enabled")
    private val KEY_TTS_ENABLED = booleanPreferencesKey("tts_enabled")
    private val KEY_TTS_SPEED = floatPreferencesKey("tts_speed")
    private val KEY_TTS_PITCH = floatPreferencesKey("tts_pitch")
    private val KEY_AUTO_SAVE_DRAFTS = booleanPreferencesKey("auto_save_drafts")
    private val KEY_MARKDOWN_RENDERING = booleanPreferencesKey("markdown_rendering")
    private val KEY_SHOW_TIMESTAMPS = booleanPreferencesKey("show_timestamps")
    private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
    private val KEY_TELEMETRY_ENABLED = booleanPreferencesKey("telemetry_enabled")
    private val KEY_DEFAULT_MODEL = stringPreferencesKey("default_model")
    private val KEY_MAX_TOKENS = intPreferencesKey("max_tokens")
    private val KEY_TEMPERATURE = floatPreferencesKey("temperature")
    
    // Theme mode
    enum class ThemeMode { SYSTEM, LIGHT, DARK }
    
    val themeModeFlow: Flow<ThemeMode> = appContext.dataStore.data.map { prefs ->
        when (prefs[KEY_THEME_MODE]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }
    
    // Font size
    val fontSizeFlow: Flow<Float> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_FONT_SIZE] ?: 1.0f
    }
    
    suspend fun setFontSize(size: Float) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_FONT_SIZE] = size
        }
    }
    
    // Send on enter
    val sendOnEnterFlow: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_SEND_ON_ENTER] ?: false
    }
    
    suspend fun setSendOnEnter(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_SEND_ON_ENTER] = enabled
        }
    }
    
    // Voice input
    val voiceInputEnabledFlow: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_INPUT_ENABLED] ?: true
    }
    
    suspend fun setVoiceInputEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_VOICE_INPUT_ENABLED] = enabled
        }
    }
    
    // TTS settings
    val ttsEnabledFlow: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_TTS_ENABLED] ?: false
    }
    
    suspend fun setTtsEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_TTS_ENABLED] = enabled
        }
    }
    
    val ttsSpeedFlow: Flow<Float> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_TTS_SPEED] ?: 1.0f
    }
    
    suspend fun setTtsSpeed(speed: Float) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_TTS_SPEED] = speed
        }
    }
    
    val ttsPitchFlow: Flow<Float> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_TTS_PITCH] ?: 1.0f
    }
    
    suspend fun setTtsPitch(pitch: Float) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_TTS_PITCH] = pitch
        }
    }
    
    // Other settings
    val autoSaveDraftsFlow: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_SAVE_DRAFTS] ?: true
    }
    
    suspend fun setAutoSaveDrafts(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_AUTO_SAVE_DRAFTS] = enabled
        }
    }
    
    val markdownRenderingFlow: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_MARKDOWN_RENDERING] ?: true
    }
    
    suspend fun setMarkdownRendering(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_MARKDOWN_RENDERING] = enabled
        }
    }
    
    val showTimestampsFlow: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_TIMESTAMPS] ?: true
    }
    
    suspend fun setShowTimestamps(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_SHOW_TIMESTAMPS] = enabled
        }
    }
    
    val notificationEnabledFlow: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATION_ENABLED] ?: true
    }
    
    suspend fun setNotificationEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATION_ENABLED] = enabled
        }
    }
    
    val telemetryEnabledFlow: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_TELEMETRY_ENABLED] ?: false
    }
    
    suspend fun setTelemetryEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_TELEMETRY_ENABLED] = enabled
        }
    }
    
    // AI Model settings
    val defaultModelFlow: Flow<String?> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_MODEL]
    }
    
    suspend fun setDefaultModel(model: String?) {
        appContext.dataStore.edit { prefs ->
            if (model != null) {
                prefs[KEY_DEFAULT_MODEL] = model
            } else {
                prefs.remove(KEY_DEFAULT_MODEL)
            }
        }
    }
    
    val maxTokensFlow: Flow<Int> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_MAX_TOKENS] ?: 2048
    }
    
    suspend fun setMaxTokens(tokens: Int) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_MAX_TOKENS] = tokens
        }
    }
    
    val temperatureFlow: Flow<Float> = appContext.dataStore.data.map { prefs ->
        prefs[KEY_TEMPERATURE] ?: 0.7f
    }
    
    suspend fun setTemperature(temperature: Float) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_TEMPERATURE] = temperature
        }
    }
}