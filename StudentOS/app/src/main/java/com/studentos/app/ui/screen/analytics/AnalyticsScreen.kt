package com.studentos.app.ui.screen.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import com.studentos.app.domain.usecase.analytics.GetAnalyticsUseCase
import com.studentos.app.ui.components.*
import com.studentos.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val subjectAnalytics: List<Pair<Subject, SubjectAnalytics>> = emptyList(),
    val overallAttendance: Double = 0.0,
    val overallAverage: Double = 0.0,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val overdueTasks: Int = 0,
    val upcomingExams: Int = 0,
    val selectedPeriod: Int = 0, // 0=week, 1=month, 2=semester
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsUseCase: GetAnalyticsUseCase,
    private val subjectRepo: SubjectRepository,
    private val assignmentRepo: AssignmentRepository,
    private val examRepo: ExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val analytics = analyticsUseCase()
                val subjects = subjectRepo.getAllOnce()
                val paired = analytics.mapNotNull { a ->
                    subjects.find { it.id == a.subjectId }?.let { it to a }
                }

                val allTasks = assignmentRepo.getAllOnce()
                val completed = allTasks.count { it.status == AssignmentStatus.DONE }
                val overdue = assignmentRepo.getOverdueCount()
                val upcomingExams = examRepo.getUpcomingOnce().size

                val overallAttendance = if (analytics.isNotEmpty())
                    analytics.map { it.attendancePercentage }.average() else 0.0
                val overallAverage = if (analytics.isNotEmpty())
                    analytics.map { it.weightedAverage }.average() else 0.0

                _uiState.value = AnalyticsUiState(
                    subjectAnalytics = paired,
                    overallAttendance = overallAttendance,
                    overallAverage = overallAverage,
                    completedTasks = completed,
                    totalTasks = allTasks.size,
                    overdueTasks = overdue,
                    upcomingExams = upcomingExams,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun setPeriod(period: Int) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }
}

@Composable
fun AnalyticsScreen(
    onNavigate: (String) -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            TopAppBar(
                title = {
                    Text(
                        "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }

        // Period selector
        item {
            val periods = listOf("Week", "Month", "Semester")
            SegmentedControl(
                items = periods,
                selectedIndex = uiState.selectedPeriod,
                onSelected = { viewModel.setPeriod(it) },
                modifier = Modifier.padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm)
            )
        }

        // Overview cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    label = "Attendance",
                    value = "${uiState.overallAttendance.toInt()}%",
                    icon = Icons.Outlined.HowToReg,
                    color = when {
                        uiState.overallAttendance >= 80 -> AppTheme.extendedColors.success
                        uiState.overallAttendance >= 70 -> Warning
                        else -> Error
                    }
                )
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    label = "Average",
                    value = "${uiState.overallAverage.toInt()}%",
                    icon = Icons.Outlined.Grade,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    label = "Tasks Done",
                    value = "${uiState.completedTasks}/${uiState.totalTasks}",
                    icon = Icons.Outlined.TaskAlt,
                    color = MaterialTheme.colorScheme.secondary
                )
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    label = "Overdue",
                    value = "${uiState.overdueTasks}",
                    icon = Icons.Outlined.Warning,
                    color = if (uiState.overdueTasks > 0) Error else AppTheme.extendedColors.success
                )
            }
        }

        // Subject breakdown
        if (uiState.subjectAnalytics.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "By Subject",
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }

            items(uiState.subjectAnalytics) { (subject, analytics) ->
                SubjectAnalyticsCard(
                    subject = subject,
                    analytics = analytics,
                    onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.SubjectAnalytics.createRoute(subject.id)) }
                )
            }
        }

        // Attendance chart
        if (uiState.subjectAnalytics.isNotEmpty()) {
            item {
                SectionHeader(title = "Attendance Overview", modifier = Modifier.padding(top = Spacing.md))
            }
            item {
                AttendanceBarChart(
                    data = uiState.subjectAnalytics.map { (s, a) -> s.name to a.attendancePercentage },
                    modifier = Modifier.padding(horizontal = ContentPadding.horizontal, vertical = Spacing.sm)
                )
            }
        }

        // Empty state
        if (!uiState.isLoading && uiState.subjectAnalytics.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Analytics,
                    title = "No data yet",
                    description = "Add subjects, attend classes, and complete tasks to see your analytics.",
                    modifier = Modifier.padding(top = Spacing.xxl)
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadius.md),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun SubjectAnalyticsCard(
    subject: Subject,
    analytics: SubjectAnalytics,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.xs),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(Spacing.base)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(subject.color).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.shortName.ifEmpty { subject.name.take(1) },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(subject.color)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${analytics.attendancePercentage.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            analytics.attendancePercentage >= 80 -> AppTheme.extendedColors.success
                            analytics.attendancePercentage >= 70 -> Warning
                            else -> Error
                        }
                    )
                    Text("Attendance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${analytics.weightedAverage.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Average", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${analytics.completedAssignments}/${analytics.totalAssignments}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text("Tasks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Attendance bar
            Spacer(modifier = Modifier.height(Spacing.md))
            LinearProgressIndicator(
                progress = { (analytics.attendancePercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = when {
                    analytics.attendancePercentage >= 80 -> AppTheme.extendedColors.success
                    analytics.attendancePercentage >= 70 -> Warning
                    else -> Error
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun AttendanceBarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val colors = SubjectColors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(Spacing.base)) {
            data.forEachIndexed { index, (name, percentage) ->
                val color = colors[index % colors.size]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name.take(10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(80.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = color.copy(alpha = 0.1f),
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}
