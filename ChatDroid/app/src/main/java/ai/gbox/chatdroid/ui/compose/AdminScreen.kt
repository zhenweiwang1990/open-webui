package ai.gbox.chatdroid.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Data models for admin functionality
data class UserInfo(
    val id: String,
    val email: String,
    val name: String?,
    val role: String,
    val isActive: Boolean,
    val lastActive: Long?,
    val createdAt: Long
)

data class SystemStats(
    val totalUsers: Int,
    val activeUsers: Int,
    val totalChats: Int,
    val totalMessages: Int,
    val storageUsed: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Users", "System", "Settings")
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text("Admin Panel") },
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
        
        // Tab row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab content
        when (selectedTab) {
            0 -> AdminOverviewTab()
            1 -> UserManagementTab()
            2 -> SystemManagementTab()
            3 -> AdminSettingsTab()
        }
    }
}

@Composable
fun AdminOverviewTab() {
    val stats = remember {
        SystemStats(
            totalUsers = 156,
            activeUsers = 89,
            totalChats = 2847,
            totalMessages = 18592,
            storageUsed = "2.4 GB"
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "System Overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Total Users",
                    value = stats.totalUsers.toString(),
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Active Users",
                    value = stats.activeUsers.toString(),
                    icon = Icons.Default.PersonAdd,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Total Chats",
                    value = stats.totalChats.toString(),
                    icon = Icons.Default.Chat,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Messages",
                    value = stats.totalMessages.toString(),
                    icon = Icons.Default.Message,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            StatsCard(
                title = "Storage Used",
                value = stats.storageUsed,
                icon = Icons.Default.Storage,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminActionButton(
                    title = "Export System Data",
                    description = "Export all system data for backup",
                    icon = Icons.Default.Download,
                    onClick = { /* TODO: Implement export */ }
                )
                
                AdminActionButton(
                    title = "System Maintenance",
                    description = "Run system cleanup and optimization",
                    icon = Icons.Default.Build,
                    onClick = { /* TODO: Implement maintenance */ }
                )
                
                AdminActionButton(
                    title = "View System Logs",
                    description = "Access system and error logs",
                    icon = Icons.Default.List,
                    onClick = { /* TODO: Implement logs viewer */ }
                )
            }
        }
    }
}

@Composable
fun UserManagementTab() {
    val users = remember {
        listOf(
            UserInfo(
                id = "1",
                email = "admin@example.com",
                name = "Admin User",
                role = "admin",
                isActive = true,
                lastActive = System.currentTimeMillis() / 1000,
                createdAt = System.currentTimeMillis() / 1000 - 86400 * 30
            ),
            UserInfo(
                id = "2",
                email = "user@example.com",
                name = "Regular User",
                role = "user",
                isActive = true,
                lastActive = System.currentTimeMillis() / 1000 - 3600,
                createdAt = System.currentTimeMillis() / 1000 - 86400 * 15
            ),
            UserInfo(
                id = "3",
                email = "inactive@example.com",
                name = "Inactive User",
                role = "user",
                isActive = false,
                lastActive = System.currentTimeMillis() / 1000 - 86400 * 7,
                createdAt = System.currentTimeMillis() / 1000 - 86400 * 60
            )
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Users (${users.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                FilledTonalButton(
                    onClick = { /* TODO: Add user */ }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add User")
                }
            }
        }
        
        items(users) { user ->
            UserListItem(
                user = user,
                onUserClick = { /* TODO: Edit user */ },
                onToggleStatus = { /* TODO: Toggle user status */ },
                onDeleteUser = { /* TODO: Delete user */ }
            )
        }
    }
}

@Composable
fun SystemManagementTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "System Management",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            AdminActionButton(
                title = "Database Backup",
                description = "Create a backup of the database",
                icon = Icons.Default.Backup,
                onClick = { /* TODO: Implement backup */ }
            )
        }
        
        item {
            AdminActionButton(
                title = "Clear Cache",
                description = "Clear system cache and temporary files",
                icon = Icons.Default.ClearAll,
                onClick = { /* TODO: Implement cache clear */ }
            )
        }
        
        item {
            AdminActionButton(
                title = "Update Models",
                description = "Refresh available AI models",
                icon = Icons.Default.Refresh,
                onClick = { /* TODO: Implement model update */ }
            )
        }
        
        item {
            AdminActionButton(
                title = "System Health Check",
                description = "Run comprehensive system diagnostics",
                icon = Icons.Default.HealthAndSafety,
                onClick = { /* TODO: Implement health check */ }
            )
        }
        
        item {
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Reset System",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "This will delete all user data and reset the system to defaults. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = { /* TODO: Implement system reset */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Reset System")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Admin Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            SettingsCard {
                SwitchSetting(
                    title = "User Registration",
                    description = "Allow new users to register",
                    checked = true,
                    onCheckedChange = { /* TODO: Toggle registration */ }
                )
            }
        }
        
        item {
            SettingsCard {
                SwitchSetting(
                    title = "Email Verification",
                    description = "Require email verification for new users",
                    checked = false,
                    onCheckedChange = { /* TODO: Toggle email verification */ }
                )
            }
        }
        
        item {
            SettingsCard {
                SwitchSetting(
                    title = "System Monitoring",
                    description = "Enable detailed system monitoring and logging",
                    checked = true,
                    onCheckedChange = { /* TODO: Toggle monitoring */ }
                )
            }
        }
        
        item {
            SettingsCard {
                SwitchSetting(
                    title = "Automatic Backups",
                    description = "Automatically backup system data daily",
                    checked = true,
                    onCheckedChange = { /* TODO: Toggle auto backups */ }
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun AdminActionButton(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
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
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun UserListItem(
    user: UserInfo,
    onUserClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onDeleteUser: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onUserClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar placeholder
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (user.name?.first() ?: user.email.first()).toString().uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name ?: user.email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(
                        text = user.role.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (user.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onUserClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    
                    DropdownMenuItem(
                        text = { Text(if (user.isActive) "Deactivate" else "Activate") },
                        onClick = {
                            showMenu = false
                            onToggleStatus()
                        },
                        leadingIcon = { 
                            Icon(
                                if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle, 
                                contentDescription = null
                            ) 
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDeleteUser()
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            ) 
                        }
                    )
                }
            }
        }
    }
}