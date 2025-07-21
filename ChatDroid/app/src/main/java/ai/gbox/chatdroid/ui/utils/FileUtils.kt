package ai.gbox.chatdroid.ui.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    
    data class FileInfo(
        val name: String,
        val size: Long,
        val mimeType: String?,
        val uri: Uri
    )
    
    /**
     * Get file information from URI
     */
    fun getFileInfo(context: Context, uri: Uri): FileInfo? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                
                if (it.moveToFirst()) {
                    val name = it.getString(nameIndex) ?: "unknown_file"
                    val size = it.getLong(sizeIndex)
                    val mimeType = context.contentResolver.getType(uri)
                    
                    FileInfo(name, size, mimeType, uri)
                } else null
            }
        } catch (e: Exception) {
            Log.e("FileUtils", "Error getting file info", e)
            null
        }
    }
    
    /**
     * Copy file from URI to internal storage
     */
    suspend fun copyFileToInternal(context: Context, uri: Uri, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.use { input ->
                    val file = File(context.filesDir, fileName)
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                    file
                }
            } catch (e: Exception) {
                Log.e("FileUtils", "Error copying file", e)
                null
            }
        }
    }
    
    /**
     * Create multipart body for file upload
     */
    fun createMultipartBody(file: File, paramName: String = "file"): MultipartBody.Part {
        val mimeType = getMimeType(file.extension) ?: "application/octet-stream"
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, file.name, requestBody)
    }
    
    /**
     * Get MIME type from file extension
     */
    private fun getMimeType(extension: String): String? {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "mp3" -> "audio/mpeg"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            else -> null
        }
    }
    
    /**
     * Check if file is an image
     */
    fun isImage(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true
    }
    
    /**
     * Check if file is a video
     */
    fun isVideo(mimeType: String?): Boolean {
        return mimeType?.startsWith("video/") == true
    }
    
    /**
     * Check if file is audio
     */
    fun isAudio(mimeType: String?): Boolean {
        return mimeType?.startsWith("audio/") == true
    }
    
    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        
        return when {
            bytes >= gb -> String.format("%.1f GB", bytes.toDouble() / gb)
            bytes >= mb -> String.format("%.1f MB", bytes.toDouble() / mb)
            bytes >= kb -> String.format("%.1f KB", bytes.toDouble() / kb)
            else -> "$bytes B"
        }
    }
    
    /**
     * Check if file size is within limits
     */
    fun isFileSizeValid(bytes: Long, maxSizeMB: Int = 10): Boolean {
        val maxBytes = maxSizeMB * 1024 * 1024
        return bytes <= maxBytes
    }
    
    /**
     * Get file extension from filename
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast(".", "")
    }
    
    /**
     * Clean up temporary files
     */
    fun cleanupTempFiles(context: Context) {
        try {
            val tempDir = File(context.filesDir, "temp")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FileUtils", "Error cleaning up temp files", e)
        }
    }
}