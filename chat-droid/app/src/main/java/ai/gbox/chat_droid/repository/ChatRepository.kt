package ai.gbox.chat_droid.repository

import ai.gbox.chat_droid.network.ChatTitle
import ai.gbox.chat_droid.network.NetworkModule

class ChatRepository(private val apiService: ai.gbox.chat_droid.network.ApiService = NetworkModule.apiService) {

    suspend fun getChatList(token: String): List<ChatTitle> {
        val authHeader = "Bearer $token"
        return apiService.getChatList(authHeader)
    }
} 