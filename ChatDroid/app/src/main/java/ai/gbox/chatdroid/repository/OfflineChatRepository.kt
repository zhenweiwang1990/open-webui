package ai.gbox.chatdroid.repository

import android.content.Context
import android.util.Log
import ai.gbox.chatdroid.database.*
import ai.gbox.chatdroid.network.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class OfflineChatRepository(
    private val context: Context,
    private val remoteRepository: ChatRepository,
    private val database: ChatDatabase = ChatDatabase.getDatabase(context)
) {
    
    private val chatDao = database.chatDao()
    private val messageDao = database.messageDao()
    private val attachmentDao = database.attachmentDao()
    
    /**
     * Get chats with offline-first approach
     */
    fun getChats(): Flow<List<ChatTitleIdResponse>> = flow {
        // First emit cached data
        chatDao.getActiveChatsWithMessageCount().collect { cachedChats ->
            emit(cachedChats.map { it.toChatTitleIdResponse() })
            
            // Then try to refresh from network
            try {
                val remoteResult = remoteRepository.fetchChats()
                remoteResult.onSuccess { remoteChats ->
                    // Update local cache
                    withContext(Dispatchers.IO) {
                        remoteChats.forEach { remoteChat ->
                            chatDao.insertChat(remoteChat.toChatEntity())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("OfflineChatRepository", "Failed to refresh chats from network", e)
            }
        }
    }
    
    /**
     * Get messages for a chat with offline-first approach
     */
    fun getMessagesForChat(chatId: String): Flow<List<MessageItem>> = flow {
        messageDao.getMessagesForChat(chatId).collect { cachedMessages ->
            emit(cachedMessages.map { it.toMessageItem() })
            
            // Try to refresh from network
            try {
                val remoteResult = remoteRepository.getChatById(chatId)
                remoteResult.onSuccess { remoteChatResponse ->
                    withContext(Dispatchers.IO) {
                        // Parse and save messages from remote chat
                        val messages = parseMessagesFromChatResponse(remoteChatResponse)
                        messageDao.insertMessages(messages.map { it.toMessageEntity(chatId) })
                    }
                }
            } catch (e: Exception) {
                Log.w("OfflineChatRepository", "Failed to refresh messages from network", e)
            }
        }
    }
    
    /**
     * Send message with offline support
     */
    suspend fun sendMessage(chatId: String, content: String): Result<String> {
        // Create local message immediately
        val userMessage = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            role = "user",
            content = content,
            timestamp = System.currentTimeMillis() / 1000
        )
        
        return withContext(Dispatchers.IO) {
            try {
                // Save user message locally first
                messageDao.insertMessage(userMessage)
                
                // Try to send to remote
                val remoteResult = remoteRepository.sendMessage(chatId, content)
                
                remoteResult.fold(
                    onSuccess = { response ->
                        // Message sent successfully
                        Log.d("OfflineChatRepository", "Message sent successfully")
                        Result.success(response)
                    },
                    onFailure = { exception ->
                        // Mark message as failed or queue for retry
                        Log.w("OfflineChatRepository", "Failed to send message, keeping local copy", exception)
                        Result.failure(exception)
                    }
                )
            } catch (e: Exception) {
                Log.e("OfflineChatRepository", "Error sending message", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Create new chat
     */
    suspend fun createNewChat(title: String = "New Chat"): Result<ChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Try remote first
                val remoteResult = remoteRepository.createNewChat(title)
                
                remoteResult.fold(
                    onSuccess = { remoteChatResponse ->
                        // Save to local database
                        chatDao.insertChat(remoteChatResponse.toChatEntity())
                        Result.success(remoteChatResponse)
                    },
                    onFailure = { exception ->
                        // Create local-only chat as fallback
                        val localChat = ChatEntity(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            createdAt = System.currentTimeMillis() / 1000,
                            updatedAt = System.currentTimeMillis() / 1000
                        )
                        chatDao.insertChat(localChat)
                        
                        Log.w("OfflineChatRepository", "Created local-only chat", exception)
                        Result.failure(exception)
                    }
                )
            } catch (e: Exception) {
                Log.e("OfflineChatRepository", "Error creating chat", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Delete chat
     */
    suspend fun deleteChat(chatId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Delete locally first
                chatDao.deleteChatById(chatId)
                
                // Try to delete remotely
                val remoteResult = remoteRepository.deleteChat(chatId)
                
                remoteResult.fold(
                    onSuccess = { success ->
                        Log.d("OfflineChatRepository", "Chat deleted successfully")
                        Result.success(success)
                    },
                    onFailure = { exception ->
                        Log.w("OfflineChatRepository", "Failed to delete chat remotely, kept local deletion", exception)
                        Result.success(true) // Local deletion succeeded
                    }
                )
            } catch (e: Exception) {
                Log.e("OfflineChatRepository", "Error deleting chat", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Search chats locally
     */
    suspend fun searchChats(query: String): Result<List<ChatTitleIdResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val localResults = chatDao.searchChats(query)
                Result.success(localResults.map { it.toChatTitleIdResponse() })
            } catch (e: Exception) {
                Log.e("OfflineChatRepository", "Error searching chats", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get offline chat count
     */
    suspend fun getOfflineChatCount(): Int {
        return withContext(Dispatchers.IO) {
            try {
                chatDao.getAllChatsWithMessageCount().first().size
            } catch (e: Exception) {
                0
            }
        }
    }
    
    /**
     * Clear all offline data
     */
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
    
    // Helper extension functions
    private fun ChatTitleIdResponse.toChatEntity(): ChatEntity {
        return ChatEntity(
            id = this.id,
            title = this.title,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            archived = this.archived,
            pinned = this.pinned
        )
    }
    
    private fun ChatResponse.toChatEntity(): ChatEntity {
        return ChatEntity(
            id = this.id,
            title = this.title,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            archived = this.archived,
            pinned = this.pinned,
            shareId = this.shareId,
            folderId = this.folderId
        )
    }
    
    private fun ChatWithMessageCount.toChatTitleIdResponse(): ChatTitleIdResponse {
        return ChatTitleIdResponse(
            id = chat.id,
            title = chat.title,
            createdAt = chat.createdAt,
            updatedAt = chat.updatedAt,
            archived = chat.archived,
            pinned = chat.pinned
        )
    }
    
    private fun ChatEntity.toChatTitleIdResponse(): ChatTitleIdResponse {
        return ChatTitleIdResponse(
            id = this.id,
            title = this.title,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            archived = this.archived,
            pinned = this.pinned
        )
    }
    
    private fun MessageEntity.toMessageItem(): MessageItem {
        return MessageItem(
            id = this.id,
            role = this.role,
            content = this.content,
            timestamp = this.timestamp,
            isLoading = this.isLoading,
            model = this.model
        )
    }
    
    private fun MessageItem.toMessageEntity(chatId: String): MessageEntity {
        return MessageEntity(
            id = this.id,
            chatId = chatId,
            role = this.role,
            content = this.content,
            timestamp = this.timestamp,
            isLoading = this.isLoading,
            model = this.model
        )
    }
    
    private fun parseMessagesFromChatResponse(chatResponse: ChatResponse): List<MessageItem> {
        val messageItems = mutableListOf<MessageItem>()
        val history = chatResponse.chat.history
        
        // Start from the current message and traverse the conversation tree
        var currentId = history.currentId
        val visitedIds = mutableSetOf<String>()
        
        while (currentId != null && currentId !in visitedIds) {
            val message = history.messages[currentId]
            if (message != null) {
                messageItems.add(0, MessageItem.fromMessage(message))
                visitedIds.add(currentId)
                currentId = message.parentId
            } else {
                break
            }
        }
        
        return messageItems
    }
}