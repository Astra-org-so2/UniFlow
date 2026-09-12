package com.studentos.app.ui.screen.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentos.app.domain.model.*
import com.studentos.app.ui.components.*
import com.studentos.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigate: (String) -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.TaskAdd.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Add task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Tasks",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // Search
            AppSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                placeholder = "Search tasks...",
                modifier = Modifier.padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm)
            )

            // Filter chips
            FilterChipGroup(
                items = TaskFilter.entries.toList(),
                selectedItem = uiState.selectedFilter,
                onSelected = { viewModel.setFilter(it) },
                label = { it.label },
                modifier = Modifier.padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm)
            )

            // Task count
            if (uiState.filteredTasks.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.filteredTasks.size} task${if (uiState.filteredTasks.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val overdue = uiState.filteredTasks.count {
                        it.assignment.status != AssignmentStatus.DONE &&
                        it.assignment.deadline != null &&
                        it.assignment.deadline < LocalDate.now()
                    }
                    if (overdue > 0) {
                        Text(
                            text = "$overdue overdue",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Tasks list
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.filteredTasks.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.TaskAlt,
                    title = "No tasks",
                    description = if (uiState.searchQuery.isNotEmpty()) "No tasks match your search."
                    else "Add your first task to stay on top of your assignments.",
                    actionLabel = "Add task",
                    onAction = { onNavigate(com.studentos.app.ui.navigation.Screen.TaskAdd.route) }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = ContentPadding.horizontal,
                        vertical = Spacing.sm,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.filteredTasks, key = { it.assignment.id }) { task ->
                        TaskCard(
                            taskWithSubject = task,
                            onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.TaskDetail.createRoute(task.assignment.id)) },
                            onComplete = { viewModel.markComplete(task.assignment.id) },
                            onDelete = { viewModel.deleteTask(task.assignment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    taskWithSubject: TaskWithSubject,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val task = taskWithSubject
    val isOverdue = task.assignment.status != AssignmentStatus.DONE &&
        task.assignment.deadline != null &&
        task.assignment.deadline < LocalDate.now()
    val isDone = task.assignment.status == AssignmentStatus.DONE
    var showMenu by remember { mutableStateOf(false) }

    val priorityColor = when (task.assignment.priority) {
        Priority.LOW -> PriorityLow
        Priority.MEDIUM -> PriorityMedium
        Priority.HIGH -> PriorityHigh
        Priority.URGENT -> PriorityUrgent
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = if (isOverdue) Error.copy(alpha = 0.04f)
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox
            Checkbox(
                checked = isDone,
                onCheckedChange = { if (!isDone) onComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AppTheme.extendedColors.success,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.padding(top = Spacing.xxs)
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    // Priority dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Text(
                        text = task.assignment.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (isDone) TextDecoration.LineThrough else null,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Subject and deadline
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.subject != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(task.subject.color))
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = task.subject.shortName.ifEmpty { task.subject.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (task.assignment.deadline != null) {
                        val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), task.assignment.deadline)
                        val deadlineText = when {
                            daysUntil < 0 -> "Overdue ${-daysUntil}d"
                            daysUntil == 0L -> "Today"
                            daysUntil == 1L -> "Tomorrow"
                            daysUntil <= 7 -> "In ${daysUntil}d"
                            else -> task.assignment.deadline.format(DateTimeFormatter.ofPattern("MMM d"))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isOverdue) Error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = deadlineText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOverdue) Error else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isOverdue) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }

                    if (task.assignment.estimatedDuration > 0) {
                        Text(
                            text = "${task.assignment.estimatedDuration}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Subtasks progress
                if (task.assignment.subtasks.isNotEmpty()) {
                    val completedSubtasks = task.assignment.subtasks.count { it.isCompleted }
                    val total = task.assignment.subtasks.size
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { completedSubtasks.toFloat() / total },
                            modifier = Modifier
                                .width(60.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AppTheme.extendedColors.success,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "$completedSubtasks/$total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        "More",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (!isDone) {
                        DropdownMenuItem(
                            text = { Text("Mark complete") },
                            onClick = { showMenu = false; onComplete() },
                            leadingIcon = { Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onClick() },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Error) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Error, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }
    }
}
