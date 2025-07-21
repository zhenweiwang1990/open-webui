package ai.gbox.chatdroid.network

import android.util.Log
import com.squareup.moshi.Json
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketService {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    
    private val _messageChannel = Channel<String>(Channel.UNLIMITED)
    val messageFlow: Flow<String> = _messageChannel.receiveAsFlow()
    
    private val _connectionStatus = Channel<ConnectionStatus>(Channel.UNLIMITED)
    val connectionStatus: Flow<ConnectionStatus> = _connectionStatus.receiveAsFlow()
    
    sealed class ConnectionStatus {
        object Connecting : ConnectionStatus()
        object Connected : ConnectionStatus()
        object Disconnected : ConnectionStatus()
        data class Error(val message: String) : ConnectionStatus()
    }
    
    fun connect(url: String, token: String? = null) {
        Log.d("WebSocketService", "Connecting to WebSocket: $url")
        
        val requestBuilder = Request.Builder().url(url)
        
        // Add authorization header if token is provided
        token?.let { 
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        
        val request = requestBuilder.build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketService", "WebSocket connected")
                _connectionStatus.trySend(ConnectionStatus.Connected)
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketService", "Received message: $text")
                _messageChannel.trySend(text)
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocketService", "Received bytes: ${bytes.hex()}")
                _messageChannel.trySend(bytes.utf8())
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketService", "WebSocket closing: $code $reason")
                _connectionStatus.trySend(ConnectionStatus.Disconnected)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketService", "WebSocket closed: $code $reason")
                _connectionStatus.trySend(ConnectionStatus.Disconnected)
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketService", "WebSocket error", t)
                _connectionStatus.trySend(ConnectionStatus.Error(t.message ?: "Unknown error"))
            }
        })
        
        _connectionStatus.trySend(ConnectionStatus.Connecting)
    }
    
    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }
    
    fun sendMessage(message: ChatStreamRequest): Boolean {
        // Convert the request to JSON and send
        // In a real implementation, you'd use your JSON serializer
        val jsonMessage = """
            {
                "model": "${message.model}",
                "messages": ${message.messages},
                "stream": ${message.stream}
            }
        """.trimIndent()
        
        return sendMessage(jsonMessage)
    }
    
    fun disconnect() {
        Log.d("WebSocketService", "Disconnecting WebSocket")
        webSocket?.close(1000, "Normal closure")
        webSocket = null
    }
    
    fun isConnected(): Boolean {
        return webSocket != null
    }
}

// Data classes for WebSocket streaming
data class ChatStreamRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
    val temperature: Float? = null,
    val maxTokens: Int? = null
)

data class ChatStreamResponse(
    val id: String,
    @field:Json(name = "object")
    val objectType: String,
    val created: Long,
    val model: String,
    val choices: List<StreamChoice>,
    val usage: Usage? = null
)

data class StreamChoice(
    val index: Int,
    val delta: ChatMessage,
    val finishReason: String?
)

data class StreamDelta(
    val role: String? = null,
    val content: String? = null
)