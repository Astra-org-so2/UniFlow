package com.studentos.app.ui.screen.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studentos.app.ui.components.*
import com.studentos.app.ui.theme.*

@Composable
fun StudyScreen(
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        "Study",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }

        // Navigation items
        item {
            StudyNavItem(
                icon = Icons.Outlined.MenuBook,
                title = "Subjects",
                subtitle = "Manage your subjects and view dashboards",
                color = MaterialTheme.colorScheme.primary,
                onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Subjects.route) }
            )
        }
        item {
            StudyNavItem(
                icon = Icons.Outlined.Person,
                title = "Teachers",
                subtitle = "View teacher information and contacts",
                color = MaterialTheme.colorScheme.secondary,
                onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Teachers.route) }
            )
        }
        item {
            StudyNavItem(
                icon = Icons.Outlined.Grade,
                title = "Grades",
                subtitle = "Track your grades and academic progress",
                color = AppTheme.extendedColors.success,
                onClick = { onNavigate("grades_all") }
            )
        }
        item {
            StudyNavItem(
                icon = Icons.Outlined.Assignment,
                title = "Exams",
                subtitle = "Upcoming exams and preparation tracking",
                color = Error,
                onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Exams.route) }
            )
        }
        item {
            StudyNavItem(
                icon = Icons.Outlined.HowToReg,
                title = "Attendance",
                subtitle = "Monitor your attendance across subjects",
                color = Warning,
                onClick = { onNavigate("attendance_all") }
            )
        }
        item {
            StudyNavItem(
                icon = Icons.Outlined.StickyNote2,
                title = "Notes",
                subtitle = "Your study notes and materials",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Notes.route) }
            )
        }
    }
}

@Composable
private fun StudyNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ListItem(
        headline = title,
        supporting = subtitle,
        leading = { IconBadge(icon = icon, color = color, size = 44.dp) },
        trailing = {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = Spacing.sm)
    )
}
