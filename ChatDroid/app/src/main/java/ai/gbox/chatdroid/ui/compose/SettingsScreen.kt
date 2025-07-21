package ai.gbox.chatdroid.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.gbox.chatdroid.datastore.AppPreferences
import ai.gbox.chatdroid.network.ModelInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    availableModels: List<ModelInfo> = emptyList(),
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Collect all settings
    val themeMode by AppPreferences.getThemeModeFlow().collectAsState(AppPreferences.ThemeMode.SYSTEM)
    val fontSize by AppPreferences.getFontSizeFlow().collectAsState(1.0f)
    val sendOnEnter by AppPreferences.getSendOnEnterFlow().collectAsState(false)
    val voiceInputEnabled by AppPreferences.getVoiceInputEnabledFlow().collectAsState(true)
    val ttsEnabled by AppPreferences.getTtsEnabledFlow().collectAsState(false)
    val ttsSpeed by AppPreferences.getTtsSpeedFlow().collectAsState(1.0f)
    val ttsPitch by AppPreferences.getTtsPitchFlow().collectAsState(1.0f)
    val autoSaveDrafts by AppPreferences.getAutoSaveDraftsFlow().collectAsState(true)
    val markdownRendering by AppPreferences.getMarkdownRenderingFlow().collectAsState(true)
    val showTimestamps by AppPreferences.getShowTimestampsFlow().collectAsState(true)
    val notificationEnabled by AppPreferences.getNotificationEnabledFlow().collectAsState(true)
    val telemetryEnabled by AppPreferences.getTelemetryEnabledFlow().collectAsState(false)
    val defaultModel by AppPreferences.getDefaultModelFlow().collectAsState(null)
    val maxTokens by AppPreferences.getMaxTokensFlow().collectAsState(2048)
    val temperature by AppPreferences.getTemperatureFlow().collectAsState(0.7f)
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    // Theme mode
                    SettingsCard {
                        Column {
                            Text(
                                text = "Theme Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(Modifier.selectableGroup()) {
                                AppPreferences.ThemeMode.values().forEach { mode ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .selectable(
                                                selected = (mode == themeMode),
                                                onClick = {
                                                    coroutineScope.launch {
                                                        AppPreferences.setThemeMode(mode)
                                                    }
                                                },
                                                role = Role.RadioButton
                                            )
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (mode == themeMode),
                                            onClick = null
                                        )
                                        Text(
                                            text = when (mode) {
                                                AppPreferences.ThemeMode.SYSTEM -> "System"
                                                AppPreferences.ThemeMode.LIGHT -> "Light"
                                                AppPreferences.ThemeMode.DARK -> "Dark"
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Font size
                    SettingsCard {
                        SliderSetting(
                            title = "Font Size",
                            value = fontSize,
                            onValueChange = { value ->
                                coroutineScope.launch {
                                    AppPreferences.setFontSize(value)
                                }
                            },
                            valueRange = 0.8f..1.5f,
                            steps = 6,
                            valueFormatter = { "${(it * 100).toInt()}%" }
                        )
                    }
                }
            }
            
            // Chat Settings
            item {
                SettingsSection(title = "Chat") {
                    SettingsCard {
                        SwitchSetting(
                            title = "Send on Enter",
                            description = "Send message when Enter key is pressed",
                            checked = sendOnEnter,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    AppPreferences.setSendOnEnter(enabled)
                                }
                            }
                        )
                    }
                    
                    SettingsCard {
                        SwitchSetting(
                            title = "Auto-save Drafts",
                            description = "Automatically save message drafts",
                            checked = autoSaveDrafts,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    AppPreferences.setAutoSaveDrafts(enabled)
                                }
                            }
                        )
                    }
                    
                    SettingsCard {
                        SwitchSetting(
                            title = "Markdown Rendering",
                            description = "Render markdown formatting in messages",
                            checked = markdownRendering,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    AppPreferences.setMarkdownRendering(enabled)
                                }
                            }
                        )
                    }
                    
                    SettingsCard {
                        SwitchSetting(
                            title = "Show Timestamps",
                            description = "Display message timestamps",
                            checked = showTimestamps,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    AppPreferences.setShowTimestamps(enabled)
                                }
                            }
                        )
                    }
                }
            }
            
            // Voice & Audio Settings
            item {
                SettingsSection(title = "Voice & Audio") {
                    SettingsCard {
                        SwitchSetting(
                            title = "Voice Input",
                            description = "Enable voice-to-text input",
                            checked = voiceInputEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    AppPreferences.setVoiceInputEnabled(enabled)
                                }
                            }
                        )
                    }
                    
                    SettingsCard {
                        SwitchSetting(
                            title = "Text-to-Speech",
                            description = "Read AI responses aloud",
                            checked = ttsEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    AppPreferences.setTtsEnabled(enabled)
                                }
                            }
                        )
                    }
                    
                    if (ttsEnabled) {
                        SettingsCard {
                            SliderSetting(
                                title = "Speech Speed",
                                value = ttsSpeed,
                                onValueChange = { value ->
                                    coroutineScope.launch {
                                        AppPreferences.setTtsSpeed(value)
                                    }
                                },
                                valueRange = 0.5f..2.0f,
                                steps = 14,
                                valueFormatter = { "${(it * 100).toInt()}%" }
                            )
                        }
                        
                        SettingsCard {
                            SliderSetting(
                                title = "Speech Pitch",
                                value = ttsPitch,
                                onValueChange = { value ->
                                    coroutineScope.launch {
                                        AppPreferences.setTtsPitch(value)
                                    }
                                },
                                valueRange = 0.5f..2.0f,
                                steps = 14,
                                valueFormatter = { "${(it * 100).toInt()}%" }
                            )
                        }
                    }
                }
            }
            
            // AI Model Settings
            item {
                SettingsSection(title = "AI Model") {
                    if (availableModels.isNotEmpty()) {
                        SettingsCard {
                            Column {
                                Text(
                                    text = "Default Model",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ModelSelector(
                                    selectedModel = availableModels.find { it.id == defaultModel },
                                    availableModels = availableModels,
                                    onModelSelected = { model ->
                                        coroutineScope.launch {
                                            AppPreferences.setDefaultModel(model.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    SettingsCard {
                        SliderSetting(
                            title = "Max Tokens",
                            value = maxTokens.toFloat(),
                            onValueChange = { value ->
                                coroutineScope.launch {
                                    AppPreferences.setMaxTokens(value.toInt())
                                }
                            },
                            valueRange = 256f..4096f,
                            steps = 15,
                            valueFormatter = { it.toInt().toString() }
                        )
                    }
                    
                    SettingsCard {
                        SliderSetting(
                            title = "Temperature",
                            value = temperature,
                            onValueChange = { value ->
                                coroutineScope.launch {
                                    AppPreferences.setTemperature(value)
                                }
                            },
                            valueRange = 0.0f..2.0f,
                            steps = 19,
                            valueFormatter = { "%.1f".format(it) }
                        )
                    }
                }
            }
            
            // Privacy & Data
            item {
                SettingsSection(title = "Privacy & Data") {
                    SettingsCard {
                        SwitchSetting(
                            title = "Notifications",
                            description = "Receive push notifications",
                            checked = notificationEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    AppPreferences.setNotificationEnabled(enabled)
                                }
                            }
                        )
                    }
                    
                    SettingsCard {
                        SwitchSetting(
                            title = "Telemetry",
                            description = "Share usage data to improve the app",
                            checked = telemetryEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    AppPreferences.setTelemetryEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SliderSetting(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueFormatter: (Float) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueFormatter(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}