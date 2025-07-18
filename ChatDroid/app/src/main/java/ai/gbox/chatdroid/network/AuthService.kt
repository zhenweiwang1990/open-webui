package ai.gbox.chatdroid.network

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("auths/signin")
    suspend fun signIn(@Body request: SignInRequest): SignInResponse
}

// Request body for /auths/signin
data class SignInRequest(
    val email: String,
    val password: String
)

// Response from /auths/signin
data class SignInResponse(
    val token: String,
    val token_type: String,
    val expires_at: Long?,
    val id: String,
    val email: String,
    val name: String?,
    val role: String?,
    val profile_image_url: String?
) 