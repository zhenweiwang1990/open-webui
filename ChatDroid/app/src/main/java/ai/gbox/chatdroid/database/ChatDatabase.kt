package ai.gbox.chatdroid.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Entity for storing chat information
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val archived: Boolean = false,
    val pinned: Boolean = false,
    val models: String = "", // JSON string of model list
    val shareId: String? = null,
    val folderId: String? = null
)

// Entity for storing individual messages
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"])]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val parentId: String? = null,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val model: String? = null,
    val timestamp: Long,
    val isLoading: Boolean = false
)

// Entity for storing file attachments
@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["messageId"])]
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val filename: String,
    val contentType: String?,
    val size: Long,
    val localPath: String? = null, // Local file path for offline access
    val remoteUrl: String? = null  // Remote URL for online access
)

// Data class for chat with message count
data class ChatWithMessageCount(
    @Embedded val chat: ChatEntity,
    val messageCount: Int,
    val lastMessageTime: Long?
)

// Data class for message with attachments
data class MessageWithAttachments(
    @Embedded val message: MessageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val attachments: List<AttachmentEntity>
)

// DAO for chat operations
@Dao
interface ChatDao {
    
    @Query("SELECT *, " +
           "(SELECT COUNT(*) FROM messages WHERE chatId = chats.id) as messageCount, " +
           "(SELECT MAX(timestamp) FROM messages WHERE chatId = chats.id) as lastMessageTime " +
           "FROM chats ORDER BY " +
           "CASE WHEN pinned = 1 THEN 0 ELSE 1 END, " +
           "updatedAt DESC")
    fun getAllChatsWithMessageCount(): Flow<List<ChatWithMessageCount>>
    
    @Query("SELECT *, " +
           "(SELECT COUNT(*) FROM messages WHERE chatId = chats.id) as messageCount, " +
           "(SELECT MAX(timestamp) FROM messages WHERE chatId = chats.id) as lastMessageTime " +
           "FROM chats WHERE archived = 0 ORDER BY " +
           "CASE WHEN pinned = 1 THEN 0 ELSE 1 END, " +
           "updatedAt DESC")
    fun getActiveChatsWithMessageCount(): Flow<List<ChatWithMessageCount>>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun getChatByIdFlow(chatId: String): Flow<ChatEntity?>
    
    @Query("SELECT * FROM chats WHERE title LIKE '%' || :query || '%'")
    suspend fun searchChats(query: String): List<ChatEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
    
    @Update
    suspend fun updateChat(chat: ChatEntity)
    
    @Delete
    suspend fun deleteChat(chat: ChatEntity)
    
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)
    
    @Query("UPDATE chats SET pinned = NOT pinned WHERE id = :chatId")
    suspend fun togglePinChat(chatId: String)
    
    @Query("UPDATE chats SET archived = NOT archived WHERE id = :chatId")
    suspend fun toggleArchiveChat(chatId: String)
}

// DAO for message operations
@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>
    
    @Transaction
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesWithAttachmentsForChat(chatId: String): Flow<List<MessageWithAttachments>>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)
    
    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)
    
    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId")
    suspend fun getMessageCountForChat(chatId: String): Int
}

// DAO for attachment operations
@Dao
interface AttachmentDao {
    
    @Query("SELECT * FROM attachments WHERE messageId = :messageId")
    suspend fun getAttachmentsForMessage(messageId: String): List<AttachmentEntity>
    
    @Query("SELECT * FROM attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: String): AttachmentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<AttachmentEntity>)
    
    @Update
    suspend fun updateAttachment(attachment: AttachmentEntity)
    
    @Delete
    suspend fun deleteAttachment(attachment: AttachmentEntity)
    
    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteAttachmentById(attachmentId: String)
    
    @Query("DELETE FROM attachments WHERE messageId = :messageId")
    suspend fun deleteAttachmentsForMessage(messageId: String)
}