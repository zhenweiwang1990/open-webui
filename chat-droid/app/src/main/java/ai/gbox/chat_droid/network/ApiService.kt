package ai.gbox.chat_droid.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

// Base URL will be http://10.0.2.2:3000/api/v1/

// ----------------------
// Data Models
// ----------------------

data class SignInRequest(
    val email: String,
    val password: String
)

data class SessionUserResponse(
    val token: String,
    val token_type: String,
    val expires_at: Long?,
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val profile_image_url: String
)

data class ChatTitle(
    val id: String,
    val title: String,
    val updated_at: Long,
    val created_at: Long
)

// ----------------------
// Retrofit API service
// ----------------------

interface ApiService {

    @POST("auths/signin")
    suspend fun signIn(@Body request: SignInRequest): SessionUserResponse

    @GET("chats/list")
    suspend fun getChatList(@Header("Authorization") authHeader: String): List<ChatTitle>
} 