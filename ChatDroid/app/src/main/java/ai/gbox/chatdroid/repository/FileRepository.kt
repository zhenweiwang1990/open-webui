package ai.gbox.chatdroid.repository

import android.util.Log
import ai.gbox.chatdroid.network.*
import ai.gbox.chatdroid.ui.utils.FileUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class FileRepository {
    private val api: FileService by lazy { ApiClient.create(FileService::class.java) }
    
    /**
     * Upload a file with progress tracking
     */
    fun uploadFile(file: File): Flow<UploadProgress> = flow {
        try {
            emit(UploadProgress.Starting)
            
            val multipartBody = FileUtils.createMultipartBody(file, "file")
            
            emit(UploadProgress.Uploading(0))
            
            val response = api.uploadFile(multipartBody)
            
            if (response.isSuccessful) {
                val uploadResponse = response.body()
                if (uploadResponse != null) {
                    Log.d("FileRepository", "File uploaded successfully: ${uploadResponse.id}")
                    emit(UploadProgress.Success(uploadResponse))
                } else {
                    emit(UploadProgress.Error("Upload response was null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FileRepository", "Upload failed: ${response.code()} ${response.message()}")
                emit(UploadProgress.Error("Upload failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Upload error", e)
            emit(UploadProgress.Error("Upload error: ${e.message}"))
        }
    }
    
    /**
     * Get list of uploaded files
     */
    suspend fun getFiles(): Result<List<FileInfo>> {
        return try {
            val response = api.getFiles()
            
            if (response.isSuccessful) {
                val files = response.body() ?: emptyList()
                Log.d("FileRepository", "Retrieved ${files.size} files")
                Result.success(files)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FileRepository", "Failed to get files: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to get files: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Error getting files", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get specific file info
     */
    suspend fun getFile(fileId: String): Result<FileInfo> {
        return try {
            val response = api.getFile(fileId)
            
            if (response.isSuccessful) {
                val file = response.body()
                if (file != null) {
                    Log.d("FileRepository", "Retrieved file: ${file.filename}")
                    Result.success(file)
                } else {
                    Result.failure(Exception("File not found"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FileRepository", "Failed to get file: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to get file: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Error getting file", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a file
     */
    suspend fun deleteFile(fileId: String): Result<Boolean> {
        return try {
            val response = api.deleteFile(fileId)
            
            if (response.isSuccessful) {
                val success = response.body() ?: false
                Log.d("FileRepository", "Delete file result: $success")
                Result.success(success)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FileRepository", "Failed to delete file: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to delete file: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Error deleting file", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download file content
     */
    suspend fun downloadFile(fileId: String): Result<ByteArray> {
        return try {
            val response = api.downloadFile(fileId)
            
            if (response.isSuccessful) {
                val bytes = response.body()?.bytes()
                if (bytes != null) {
                    Log.d("FileRepository", "Downloaded file: $fileId (${bytes.size} bytes)")
                    Result.success(bytes)
                } else {
                    Result.failure(Exception("File content was null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("FileRepository", "Failed to download file: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to download file: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Error downloading file", e)
            Result.failure(e)
        }
    }
}

sealed class UploadProgress {
    object Starting : UploadProgress()
    data class Uploading(val percentage: Int) : UploadProgress()
    data class Success(val fileResponse: FileUploadResponse) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}