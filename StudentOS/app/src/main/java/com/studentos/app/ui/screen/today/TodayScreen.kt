package com.studentos.app.ui.screen.today

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentos.app.domain.model.*
import com.studentos.app.ui.components.*
import com.studentos.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigate: (String) -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "StudentOS",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Settings.route) }) {
                        Icon(Icons.Outlined.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Greeting
                item {
                    GreetingSection(uiState.greeting)
                }

                // Next Class card
                if (uiState.nextClass != null) {
                    item {
                        NextClassCard(
                            scheduleItem = uiState.nextClass!!,
                            subject = uiState.nextClassSubject,
                            teacher = uiState.nextClassTeacher,
                            onClick = { /* navigate to detail */ }
                        )
                    }
                }

                // Today's schedule
                if (uiState.todaySchedule.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Today's Schedule",
                            action = "See all",
                            onAction = { onNavigate(com.studentos.app.ui.navigation.Screen.Schedule.route) }
                        )
                    }
                    items(uiState.todaySchedule) { item ->
                        TodayScheduleCard(
                            item = item,
                            onClick = { /* navigate */ }
                        )
                    }
                }

                // Attendance warnings
                if (uiState.attendanceWarnings.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Attendance Warnings")
                    }
                    items(uiState.attendanceWarnings) { warning ->
                        AttendanceWarningCard(warning = warning, onClick = {
                            onNavigate(com.studentos.app.ui.navigation.Screen.Attendance.createRoute(warning.subjectId))
                        })
                    }
                }

                // Upcoming tasks
                if (uiState.upcomingTasks.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Upcoming Tasks",
                            action = "See all",
                            onAction = { onNavigate(com.studentos.app.ui.navigation.Screen.Tasks.route) }
                        )
                    }
                    items(uiState.upcomingTasks) { task ->
                        TaskRow(task = task, onClick = {
                            onNavigate(com.studentos.app.ui.navigation.Screen.TaskDetail.createRoute(task.id))
                        })
                    }
                }

                // Upcoming exams
                if (uiState.upcomingExams.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Exams",
                            action = "See all",
                            onAction = { onNavigate(com.studentos.app.ui.navigation.Screen.Exams.route) }
                        )
                    }
                    items(uiState.upcomingExams) { exam ->
                        ExamRow(exam = exam, onClick = {
                            onNavigate(com.studentos.app.ui.navigation.Screen.ExamEdit.createRoute(exam.id))
                        })
                    }
                }

                // Smart recommendation
                if (uiState.recommendation != null) {
                    item {
                        RecommendationCard(recommendation = uiState.recommendation!!)
                    }
                }

                // Empty state
                if (uiState.todaySchedule.isEmpty() && uiState.upcomingTasks.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.EventNote,
                            title = "Your day is clear",
                            description = "No classes or tasks scheduled for today. Enjoy your free time or plan ahead!",
                            actionLabel = "View Schedule",
                            onAction = { onNavigate(com.studentos.app.ui.navigation.Screen.Schedule.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingSection(greeting: String) {
    Text(
        text = greeting,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm)
    )
    Text(
        text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = ContentPadding.horizontal, bottom = Spacing.lg)
    )
}

@Composable
private fun NextClassCard(
    scheduleItem: ScheduleItem,
    subject: Subject?,
    teacher: Teacher?,
    onClick: () -> Unit
) {
    val now = LocalTime.now()
    val minutesUntil = ChronoUnit.MINUTES.between(now, scheduleItem.startTime).coerceAtLeast(0)
    val isHappening = now.isAfter(scheduleItem.startTime) && now.isBefore(scheduleItem.endTime)

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm),
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        cornerRadius = AppRadius.lg
    ) {
        Column(modifier = Modifier.padding(Spacing.base)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHappening) "HAPPENING NOW" else "NEXT CLASS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (!isHappening && minutesUntil > 0) {
                    StatusChip(
                        text = "in ${minutesUntil}min",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = subject?.name ?: "Unknown",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.base),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "${scheduleItem.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} — ${scheduleItem.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                if (scheduleItem.room.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Room,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = scheduleItem.room,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            if (teacher != null) {
                Spacer(modifier = Modifier.xs)
                Text(
                    text = teacher.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}



@Composable
private fun TodayScheduleCard(
    item: TodayScheduleItem,
    onClick: () -> Unit
) {
    val isPast = LocalTime.now().isAfter(item.scheduleItem.endTime)
    val alpha = if (isPast) 0.5f else 1f

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.xs),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    text = item.scheduleItem.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Text(
                    text = item.scheduleItem.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Color indicator
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(item.subject.color).copy(alpha = alpha))
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.subject.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.scheduleItem.room.isNotEmpty()) {
                        Text(
                            text = item.scheduleItem.room,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.teacher != null) {
                        Text(
                            text = item.teacher.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Now indicator
            if (item.isNow) {
                StatusChip(text = "Now", color = MaterialTheme.colorScheme.primary)
            } else if (isPast) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Completed",
                    tint = AppTheme.extendedColors.success.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Class type badge
            if (!item.isNow && !isPast) {
                val typeLabel = when (item.scheduleItem.type) {
                    ClassType.LECTURE -> "Л"
                    ClassType.PRACTICE -> "Пр"
                    ClassType.LAB -> "Лаб"
                    ClassType.SEMINAR -> "С"
                    ClassType.OTHER -> ""
                }
                if (typeLabel.isNotEmpty()) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(AppRadius.xs)
                            )
                            .padding(horizontal = Spacing.xs, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceWarningCard(
    warning: AttendanceWarning,
    onClick: () -> Unit
) {
    val color = when {
        warning.percentage < 70 -> Error
        warning.percentage < 75 -> Warning
        else -> AppTheme.extendedColors.warning
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.xs),
        onClick = onClick,
        containerColor = color.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = Icons.Filled.Warning, color = color, size = 36.dp, iconSize = 18.dp)
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${warning.subjectName} attendance: ${warning.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${warning.safeAbsencesRemaining} safe absence${if (warning.safeAbsencesRemaining != 1) "s" else ""} remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskRow(task: Assignment, onClick: () -> Unit) {
    val isOverdue = task.deadline != null && task.deadline.isBefore(LocalDate.now()) && task.status != AssignmentStatus.DONE

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.xs),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val priorityColor = when (task.priority) {
                Priority.LOW -> PriorityLow
                Priority.MEDIUM -> PriorityMedium
                Priority.HIGH -> PriorityHigh
                Priority.URGENT -> PriorityUrgent
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isOverdue) Error else MaterialTheme.colorScheme.onSurface
                )
                if (task.deadline != null) {
                    Text(
                        text = if (isOverdue) "Overdue · ${task.deadline.format(DateTimeFormatter.ofPattern("MMM d"))}"
                        else "Due ${task.deadline.format(DateTimeFormatter.ofPattern("MMM d"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) Error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (task.estimatedDuration > 0) {
                Text(
                    text = "${task.estimatedDuration}min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExamRow(exam: Exam, onClick: () -> Unit) {
    val daysUntil = exam.daysUntil
    val urgencyColor = when {
        daysUntil <= 3 -> Error
        daysUntil <= 7 -> Warning
        else -> MaterialTheme.colorScheme.primary
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.xs),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(
                icon = if (exam.type == ExamType.EXAM) Icons.Filled.Assignment else Icons.Filled.Grade,
                color = urgencyColor,
                size = 36.dp,
                iconSize = 18.dp
            )
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.type.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = exam.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = "${daysUntil}d left",
                color = urgencyColor
            )
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: StudyRecommendation) {
    val priorityColor = when (recommendation.priority) {
        Priority.LOW -> PriorityLow
        Priority.MEDIUM -> PriorityMedium
        Priority.HIGH -> PriorityHigh
        Priority.URGENT -> PriorityUrgent
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm),
        containerColor = priorityColor.copy(alpha = 0.06f)
    ) {
        Column(modifier = Modifier.padding(Spacing.base)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BEST NEXT ACTION",
                    style = MaterialTheme.typography.labelMedium,
                    color = priorityColor,
                    fontWeight = FontWeight.Bold
                )
                if (recommendation.estimatedMinutes > 0) {
                    Text(
                        text = "~${recommendation.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            if (recommendation.reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = recommendation.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
