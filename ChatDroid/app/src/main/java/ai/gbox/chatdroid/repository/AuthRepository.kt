package ai.gbox.chatdroid.repository

import ai.gbox.chatdroid.datastore.AuthPreferences
import ai.gbox.chatdroid.network.ApiClient
import ai.gbox.chatdroid.network.AuthService
import ai.gbox.chatdroid.network.SignInRequest
import ai.gbox.chatdroid.network.SignInResponse
import kotlinx.coroutines.flow.Flow

class AuthRepository {

    private val api: AuthService by lazy { ApiClient.create(AuthService::class.java) }

    suspend fun login(email: String, password: String): Result<SignInResponse> {
        return try {
            val response = api.signIn(SignInRequest(email, password))
            AuthPreferences.saveToken(response.token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val tokenFlow: Flow<String?> = AuthPreferences.tokenFlow

    suspend fun clearSession() {
        AuthPreferences.saveToken("")
    }
} 