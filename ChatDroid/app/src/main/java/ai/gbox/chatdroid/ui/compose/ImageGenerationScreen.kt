package ai.gbox.chatdroid.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class ImageGenerationRequest(
    val prompt: String,
    val negativePrompt: String = "",
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val guidanceScale: Float = 7.5f,
    val model: String = "stable-diffusion"
)

data class GeneratedImage(
    val id: String,
    val url: String,
    val prompt: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenerationScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }
    var negativePrompt by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("512x512") }
    var steps by remember { mutableStateOf(20) }
    var guidanceScale by remember { mutableStateOf(7.5f) }
    var isGenerating by remember { mutableStateOf(false) }
    
    // Mock generated images
    val generatedImages = remember {
        mutableStateListOf<GeneratedImage>()
    }
    
    val imageSizes = listOf("512x512", "768x768", "1024x1024", "512x768", "768x512")
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text("Image Generation") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Generate Image",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        // Prompt input
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            label = { Text("Prompt") },
                            placeholder = { Text("Describe the image you want to generate...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            )
                        )
                        
                        // Negative prompt input
                        OutlinedTextField(
                            value = negativePrompt,
                            onValueChange = { negativePrompt = it },
                            label = { Text("Negative Prompt (Optional)") },
                            placeholder = { Text("What you don't want in the image...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            )
                        )
                        
                        // Image size selector
                        var expanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedSize,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Image Size") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                imageSizes.forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text(size) },
                                        onClick = {
                                            selectedSize = size
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Advanced settings
                        Text(
                            text = "Advanced Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // Steps slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Steps")
                                Text(steps.toString())
                            }
                            Slider(
                                value = steps.toFloat(),
                                onValueChange = { steps = it.toInt() },
                                valueRange = 10f..50f,
                                steps = 7
                            )
                        }
                        
                        // Guidance scale slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Guidance Scale")
                                Text("%.1f".format(guidanceScale))
                            }
                            Slider(
                                value = guidanceScale,
                                onValueChange = { guidanceScale = it },
                                valueRange = 1f..20f,
                                steps = 18
                            )
                        }
                        
                        // Generate button
                        FilledButton(
                            onClick = {
                                if (prompt.isNotBlank()) {
                                    isGenerating = true
                                    // TODO: Implement actual image generation
                                    // For now, simulate with a delay
                                    kotlin.concurrent.thread {
                                        Thread.sleep(3000)
                                        isGenerating = false
                                        // Add mock generated image
                                        generatedImages.add(
                                            GeneratedImage(
                                                id = System.currentTimeMillis().toString(),
                                                url = "https://via.placeholder.com/512x512.png?text=Generated+Image",
                                                prompt = prompt,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                            },
                            enabled = prompt.isNotBlank() && !isGenerating,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating...")
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Image")
                            }
                        }
                    }
                }
            }
            
            if (generatedImages.isNotEmpty()) {
                item {
                    Text(
                        text = "Generated Images",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                items(generatedImages.reversed()) { image ->
                    GeneratedImageCard(
                        image = image,
                        onDownload = { /* TODO: Implement download */ },
                        onShare = { /* TODO: Implement share */ },
                        onDelete = { generatedImages.remove(image) }
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratedImageCard(
    image: GeneratedImage,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Image
            AsyncImage(
                model = image.url,
                contentDescription = image.prompt,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Prompt
            Text(
                text = image.prompt,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download")
                }
                
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}