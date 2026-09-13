package com.studentos.app.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studentos.app.ui.components.*
import com.studentos.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Profile section
            item {
                SectionHeader(title = "Profile")
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Person,
                    title = "Student Profile",
                    subtitle = "Name, university, course, group",
                    onClick = { /* Navigate to profile edit */ }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Semester Settings",
                    subtitle = "Start and end dates",
                    onClick = { /* Navigate to semester settings */ }
                )
            }

            // Data section
            item {
                SectionHeader(title = "Data", modifier = Modifier.padding(top = Spacing.md))
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Backup,
                    title = "Backup & Restore",
                    subtitle = "Export and import your data",
                    onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Backup.route) }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.FileDownload,
                    title = "Import",
                    subtitle = "Import from CSV, JSON, or ICS",
                    onClick = { /* Navigate to import */ }
                )
            }

            // Appearance
            item {
                SectionHeader(title = "Appearance", modifier = Modifier.padding(top = Spacing.md))
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.DarkMode,
                    title = "Theme",
                    subtitle = "Follow system",
                    onClick = { /* Toggle theme */ }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Language,
                    title = "Language",
                    subtitle = "English",
                    onClick = { /* Toggle language */ }
                )
            }

            // Notifications
            item {
                SectionHeader(title = "Notifications", modifier = Modifier.padding(top = Spacing.md))
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Notification Settings",
                    subtitle = "Class reminders, deadline alerts",
                    onClick = { /* Navigate to notification settings */ }
                )
            }

            // Security
            item {
                SectionHeader(title = "Security", modifier = Modifier.padding(top = Spacing.md))
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    title = "App Lock",
                    subtitle = "PIN or biometric",
                    onClick = { /* Navigate to security settings */ }
                )
            }

            // AI
            item {
                SectionHeader(title = "AI Assistant", modifier = Modifier.padding(top = Spacing.md))
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.SmartToy,
                    title = "AI Provider",
                    subtitle = "Not configured",
                    onClick = { /* Navigate to AI settings */ }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Chat,
                    title = "AI Chat",
                    subtitle = "Ask questions about your studies",
                    onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.AIChat.route) }
                )
            }

            // About
            item {
                SectionHeader(title = "About", modifier = Modifier.padding(top = Spacing.md))
            }
            item {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "About StudentOS",
                    subtitle = "Version 1.0.0",
                    onClick = { /* Show about dialog */ }
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headline = title,
        supporting = subtitle,
        leading = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        },
        trailing = {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = Spacing.sm)
    )
}
