package ai.gbox.chatdroid.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface FileService {
    
    @Multipart
    @POST("files/")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("purpose") purpose: RequestBody? = null
    ): Response<FileUploadResponse>
    
    @GET("files/")
    suspend fun getFiles(): Response<List<FileInfo>>
    
    @GET("files/{fileId}")
    suspend fun getFile(@Path("fileId") fileId: String): Response<FileInfo>
    
    @DELETE("files/{fileId}")
    suspend fun deleteFile(@Path("fileId") fileId: String): Response<Boolean>
    
    @GET("files/{fileId}/content")
    suspend fun downloadFile(@Path("fileId") fileId: String): Response<ResponseBody>
}

// Data models for file operations
data class FileUploadResponse(
    val id: String,
    val filename: String,
    val meta: FileMetadata
)

data class FileInfo(
    val id: String,
    val filename: String,
    val meta: FileMetadata,
    val user_id: String,
    val created_at: Long,
    val updated_at: Long
)

data class FileMetadata(
    val content_type: String?,
    val size: Long,
    val name: String
)