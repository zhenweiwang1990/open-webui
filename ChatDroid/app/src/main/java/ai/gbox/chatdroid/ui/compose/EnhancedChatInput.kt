package ai.gbox.chatdroid.ui.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ai.gbox.chatdroid.network.ModelInfo
import ai.gbox.chatdroid.ui.utils.SpeechManager
import kotlinx.coroutines.launch

@Composable
fun EnhancedChatInput(
    messageText: String,
    onMessageTextChanged: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    selectedModel: ModelInfo?,
    availableModels: List<ModelInfo>,
    onModelSelected: (ModelInfo) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    
    // Speech manager
    val speechManager = remember { SpeechManager(context) }
    var isListening by remember { mutableStateOf(false) }
    var speechError by remember { mutableStateOf<String?>(null) }
    
    // Collect speech results
    LaunchedEffect(speechManager) {
        speechManager.speechResults.collect { result ->
            when (result) {
                is SpeechManager.SpeechResult.Success -> {
                    onMessageTextChanged(messageText + " " + result.text)
                    isListening = false
                }
                is SpeechManager.SpeechResult.Error -> {
                    speechError = result.message
                    isListening = false
                }
                is SpeechManager.SpeechResult.Listening -> {
                    isListening = true
                    speechError = null
                }
                is SpeechManager.SpeechResult.Stopped -> {
                    isListening = false
                }
            }
        }
    }
    
    // Clean up speech manager
    DisposableEffect(speechManager) {
        onDispose {
            speechManager.release()
        }
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Model selector
            if (availableModels.isNotEmpty()) {
                ModelSelectorCompact(
                    selectedModel = selectedModel,
                    availableModels = availableModels,
                    onModelSelected = onModelSelected,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Speech error display
            speechError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Message input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Text input
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChanged,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .animateContentSize(),
                    placeholder = { 
                        Text(
                            if (isListening) "Listening..." 
                            else "Type a message..."
                        ) 
                    },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank() && !isLoading) {
                                onSendMessage(messageText)
                                keyboardController?.hide()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !isLoading && !isListening,
                    trailingIcon = if (isListening) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MicOff,
                                    contentDescription = "Listening",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else null
                )
                
                // Voice input button
                IconButton(
                    onClick = {
                        if (isListening) {
                            speechManager.stopListening()
                        } else {
                            coroutineScope.launch {
                                if (speechManager.hasRecordPermission()) {
                                    speechManager.startListening()
                                } else {
                                    speechError = "Microphone permission required"
                                }
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Icon(
                        if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop recording" else "Voice input",
                        tint = if (isListening) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
                
                // Send button
                FilledIconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            keyboardController?.hide()
                        }
                    },
                    enabled = messageText.isNotBlank() && !isLoading && !isListening,
                    modifier = Modifier.size(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send message",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceRecordingIndicator(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    if (isRecording) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated recording dots
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "recording")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(index * 200)
                    ),
                    label = "alpha"
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = alpha),
                            CircleShape
                        )
                )
                
                if (index < 2) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Recording...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}