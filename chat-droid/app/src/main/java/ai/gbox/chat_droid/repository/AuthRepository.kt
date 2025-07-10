package ai.gbox.chat_droid.repository

import ai.gbox.chat_droid.network.NetworkModule
import ai.gbox.chat_droid.network.SignInRequest
import ai.gbox.chat_droid.network.SessionUserResponse

class AuthRepository(private val apiService: ai.gbox.chat_droid.network.ApiService = NetworkModule.apiService) {

    suspend fun signIn(email: String, password: String): SessionUserResponse {
        return apiService.signIn(SignInRequest(email, password))
    }
} 