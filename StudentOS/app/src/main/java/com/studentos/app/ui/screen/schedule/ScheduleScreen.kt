package com.studentos.app.ui.screen.schedule

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentos.app.domain.model.ClassType
import com.studentos.app.ui.components.*
import com.studentos.app.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigate: (String) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.ScheduleAdd.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, "Add class")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        "Schedule",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // View toggle
            val viewOptions = listOf("Day", "Week", "Month")
            SegmentedControl(
                items = viewOptions,
                selectedIndex = uiState.selectedView,
                onSelected = { viewModel.setView(it) },
                modifier = Modifier.padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm)
            )

            when (uiState.selectedView) {
                0 -> DayView(uiState, viewModel, onNavigate)
                1 -> WeekView(uiState, viewModel, onNavigate)
                2 -> MonthView(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun DayView(
    uiState: ScheduleUiState,
    viewModel: ScheduleViewModel,
    onNavigate: (String) -> Unit
) {
    Column {
        // Day selector
        DayOfWeekSelector(
            selectedDay = uiState.selectedDay,
            onDaySelected = { viewModel.selectDay(it) }
        )

        if (uiState.daySchedule.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.EventBusy,
                title = "No classes",
                description = "No classes scheduled for this day.",
                actionLabel = "Add class",
                onAction = { onNavigate(com.studentos.app.ui.navigation.Screen.ScheduleAdd.route) },
                modifier = Modifier.padding(top = Spacing.xxl)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = ContentPadding.horizontal,
                    vertical = Spacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(uiState.daySchedule) { item ->
                    ScheduleCard(
                        item = item,
                        onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.ScheduleEdit.createRoute(item.item.id)) },
                        onDelete = { viewModel.deleteScheduleItem(item.item.id) },
                        onDuplicate = { viewModel.duplicateScheduleItem(item.item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekView(
    uiState: ScheduleUiState,
    viewModel: ScheduleViewModel,
    onNavigate: (String) -> Unit
) {
    Column {
        DayOfWeekSelector(
            selectedDay = uiState.selectedDay,
            onDaySelected = { viewModel.selectDay(it) }
        )

        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = ContentPadding.horizontal,
                vertical = Spacing.md
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            val days = listOf(
                "Monday" to 1, "Tuesday" to 2, "Wednesday" to 3,
                "Thursday" to 4, "Friday" to 5, "Saturday" to 6, "Sunday" to 7
            )
            for ((name, dayNum) in days) {
                val items = uiState.weekSchedule[dayNum] ?: emptyList()
                if (items.isNotEmpty()) {
                    item {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (dayNum == uiState.selectedDay)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (dayNum == uiState.selectedDay) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                    }
                    items(items) { item ->
                        ScheduleCard(
                            item = item,
                            compact = true,
                            onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.ScheduleEdit.createRoute(item.item.id)) },
                            onDelete = { viewModel.deleteScheduleItem(item.item.id) },
                            onDuplicate = { viewModel.duplicateScheduleItem(item.item.id) }
                        )
                    }
                }
            }

            // Empty week state
            if (uiState.weekSchedule.values.all { it.isEmpty() }) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "No schedule yet",
                        description = "Add your first class to get started with your schedule.",
                        actionLabel = "Add class",
                        onAction = { onNavigate(com.studentos.app.ui.navigation.Screen.ScheduleAdd.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthView(
    uiState: ScheduleUiState,
    viewModel: ScheduleViewModel
) {
    // Simplified month view showing class counts per day
    val today = LocalDate.now()
    val firstOfMonth = today.withDayOfMonth(1)
    val daysInMonth = today.lengthOfMonth()

    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = ContentPadding.horizontal,
            vertical = Spacing.md
        )
    ) {
        items(daysInMonth) { dayIndex ->
            val date = firstOfMonth.plusDays(dayIndex.toLong())
            val dayOfWeek = date.dayOfWeek.value
            val items = uiState.weekSchedule[dayOfWeek] ?: emptyList()

            if (items.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.width(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                            color = if (date == today) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = date.dayOfWeek.name.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        modifier = Modifier.weight(1f)
                    ) {
                        items.take(4).forEach { item ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(item.subject.color))
                            )
                        }
                        if (items.size > 4) {
                            Text(
                                text = "+${items.size - 4}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${items.size} class${if (items.size > 1) "es" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DayOfWeekSelector(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val today = LocalDate.now()
    val weekStart = today.with(DayOfWeek.MONDAY)

    LazyRow(
        contentPadding = PaddingValues(horizontal = ContentPadding.horizontal, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(7) { index ->
            val dayNum = index + 1
            val date = weekStart.plusDays(index.toLong())
            val selected = dayNum == selectedDay
            val isToday = date == today

            Surface(
                onClick = { onDaySelected(dayNum) },
                shape = RoundedCornerShape(AppRadius.md),
                color = when {
                    selected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                },
                modifier = Modifier.width(48.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = Spacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = date.dayOfWeek.name.take(2),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            selected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            selected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    item: ScheduleItemWithDetails,
    compact: Boolean = false,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(if (compact) Spacing.md else Spacing.base),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (compact) 36.dp else 48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(item.subject.color))
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            // Time
            Column(
                modifier = Modifier.width(52.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = item.item.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.item.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.subject.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    if (item.item.room.isNotEmpty()) {
                        Text(
                            text = item.item.room,
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

            // Type badge
            val typeLabel = when (item.item.type) {
                ClassType.LECTURE -> "Лек"
                ClassType.PRACTICE -> "Пр"
                ClassType.LAB -> "Лаб"
                ClassType.SEMINAR -> "Сем"
                ClassType.OTHER -> ""
            }
            if (typeLabel.isNotEmpty()) {
                StatusChip(text = typeLabel, color = Color(item.subject.color))
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.MoreVert, "More", modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onClick() },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = { showMenu = false; onDuplicate() },
                        leadingIcon = { Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(18.dp)) }
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
