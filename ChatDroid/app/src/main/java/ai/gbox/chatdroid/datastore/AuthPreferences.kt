package ai.gbox.chatdroid.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

object AuthPreferences {
    private const val DATASTORE_NAME = "auth_prefs"

    private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val KEY_TOKEN = stringPreferencesKey("token")

    val tokenFlow: Flow<String?> by lazy {
        appContext.dataStore.data.map { it[KEY_TOKEN] }
    }

    suspend fun saveToken(token: String) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    suspend fun getToken(): String? {
        return appContext.dataStore.data.map { it[KEY_TOKEN] }.firstOrNull()
    }

    fun currentToken(): String? = runBlocking {
        getToken()
    }
} 