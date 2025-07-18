package ai.gbox.chatdroid.repository

import android.util.Log
import ai.gbox.chatdroid.network.*
import ai.gbox.chatdroid.datastore.AuthPreferences
import com.squareup.moshi.JsonDataException
import retrofit2.HttpException
import java.util.UUID

class ChatRepository {
    private val api: ChatService by lazy { ApiClient.create(ChatService::class.java) }

    // Get all chats
    suspend fun fetchChats(): Result<List<ChatTitleIdResponse>> {
        return try {
            Log.d("ChatRepository", "Making API call to fetch chats")
            val response = api.getChats()
            
            Log.d("ChatRepository", "API Response Code: ${response.code()}")
            Log.d("ChatRepository", "API Response Message: ${response.message()}")
            
            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                Log.d("ChatRepository", "API call successful, got ${list.size} chats")
                list.forEach { chat ->
                    Log.d("ChatRepository", "Chat: id=${chat.id}, title=${chat.title}")
                }
                Result.success(list)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "API returned error: ${response.code()} ${response.message()}")
                Log.e("ChatRepository", "Error body: $errorBody")
                Result.failure(Exception("API error: ${response.code()} ${response.message()}. Body: $errorBody"))
            }
        } catch (e: JsonDataException) {
            Log.e("ChatRepository", "JSON parsing error: ${e.message}", e)
            Result.failure(Exception("JSON parsing error: ${e.message}. The API response format may not match expected structure."))
        } catch (e: HttpException) {
            Log.e("ChatRepository", "HTTP error: ${e.code()} ${e.message()}", e)
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ChatRepository", "Error response body: $errorBody")
            } catch (ex: Exception) {
                Log.e("ChatRepository", "Could not read error body", ex)
            }
            Result.failure(Exception("API error: ${e.code()} ${e.message()}"))
        } catch (e: Exception) {
            Log.e("ChatRepository", "API call failed", e)
            Result.failure(e)
        }
    }

    // Get chat by ID
    suspend fun getChatById(chatId: String): Result<ChatResponse> {
        return try {
            Log.d("ChatRepository", "Fetching chat by ID: $chatId")
            val response = api.getChatById(chatId)
            
            if (response.isSuccessful) {
                val chat = response.body()
                if (chat != null) {
                    Log.d("ChatRepository", "Successfully fetched chat: ${chat.title}")
                    Result.success(chat)
                } else {
                    Log.e("ChatRepository", "Chat not found")
                    Result.failure(Exception("Chat not found"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "Failed to fetch chat: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to fetch chat: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching chat by ID", e)
            Result.failure(e)
        }
    }

    // Create new chat
    suspend fun createNewChat(title: String = "New Chat"): Result<ChatResponse> {
        return try {
            Log.d("ChatRepository", "Creating new chat with title: $title")
            
            // Create a minimal chat structure
            val chatData = ChatData(
                history = ChatHistory(
                    currentId = null,
                    messages = emptyMap()
                ),
                title = title
            )
            
            val chatForm = ChatForm(chat = chatData)
            val response = api.createNewChat(chatForm)
            
            if (response.isSuccessful) {
                val chat = response.body()
                if (chat != null) {
                    Log.d("ChatRepository", "Successfully created chat: ${chat.id}")
                    Result.success(chat)
                } else {
                    Log.e("ChatRepository", "Failed to create chat - null response")
                    Result.failure(Exception("Failed to create chat - null response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "Failed to create chat: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to create chat: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error creating new chat", e)
            Result.failure(e)
        }
    }

    // Send message and get completion
    suspend fun sendMessage(chatId: String, message: String, model: String = "gpt-3.5-turbo"): Result<String> {
        return try {
            Log.d("ChatRepository", "Sending message to chat $chatId: $message")
            
            val messages = listOf(
                ChatMessage(role = "user", content = message)
            )
            
            val completionRequest = ChatCompletionRequest(
                model = model,
                messages = messages,
                stream = false
            )
            
            val response = api.sendChatCompletion(completionRequest)
            
            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Log.d("ChatRepository", "Received completion response: $responseBody")
                
                if (responseBody != null) {
                    // For now, return the raw response. We'll parse this properly later
                    Result.success(responseBody)
                } else {
                    Result.failure(Exception("Empty response from chat completion"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "Failed to send message: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to send message: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            Result.failure(e)
        }
    }

    // Delete chat
    suspend fun deleteChat(chatId: String): Result<Boolean> {
        return try {
            Log.d("ChatRepository", "Deleting chat: $chatId")
            val response = api.deleteChat(chatId)
            
            if (response.isSuccessful) {
                val success = response.body() ?: false
                Log.d("ChatRepository", "Delete chat result: $success")
                Result.success(success)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "Failed to delete chat: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to delete chat: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting chat", e)
            Result.failure(e)
        }
    }

    // Get available models
    suspend fun getModels(): Result<List<ModelInfo>> {
        return try {
            Log.d("ChatRepository", "Fetching available models")
            val response = api.getModels()
            
            if (response.isSuccessful) {
                val modelsResponse = response.body()
                val models = modelsResponse?.data ?: emptyList()
                Log.d("ChatRepository", "Successfully fetched ${models.size} models")
                Result.success(models)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "Failed to fetch models: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to fetch models: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error fetching models", e)
            Result.failure(e)
        }
    }

    // Search chats
    suspend fun searchChats(query: String): Result<List<ChatTitleIdResponse>> {
        return try {
            Log.d("ChatRepository", "Searching chats with query: $query")
            val response = api.searchChats(query)
            
            if (response.isSuccessful) {
                val chats = response.body() ?: emptyList()
                Log.d("ChatRepository", "Search returned ${chats.size} chats")
                Result.success(chats)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "Failed to search chats: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to search chats: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error searching chats", e)
            Result.failure(e)
        }
    }

    // Toggle pin status
    suspend fun togglePinChat(chatId: String): Result<ChatResponse> {
        return try {
            Log.d("ChatRepository", "Toggling pin status for chat: $chatId")
            val response = api.togglePinChat(chatId)
            
            if (response.isSuccessful) {
                val chat = response.body()
                if (chat != null) {
                    Log.d("ChatRepository", "Successfully toggled pin status: ${chat.pinned}")
                    Result.success(chat)
                } else {
                    Result.failure(Exception("Failed to toggle pin - null response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "Failed to toggle pin: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to toggle pin: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error toggling pin status", e)
            Result.failure(e)
        }
    }

    // Toggle archive status
    suspend fun toggleArchiveChat(chatId: String): Result<ChatResponse> {
        return try {
            Log.d("ChatRepository", "Toggling archive status for chat: $chatId")
            val response = api.toggleArchiveChat(chatId)
            
            if (response.isSuccessful) {
                val chat = response.body()
                if (chat != null) {
                    Log.d("ChatRepository", "Successfully toggled archive status: ${chat.archived}")
                    Result.success(chat)
                } else {
                    Result.failure(Exception("Failed to toggle archive - null response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRepository", "Failed to toggle archive: ${response.code()} ${response.message()}")
                Result.failure(Exception("Failed to toggle archive: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error toggling archive status", e)
            Result.failure(e)
        }
    }
} 