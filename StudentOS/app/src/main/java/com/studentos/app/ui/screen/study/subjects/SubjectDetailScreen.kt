package com.studentos.app.ui.screen.study.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import com.studentos.app.domain.usecase.analytics.GetSubjectAnalyticsUseCase
import com.studentos.app.domain.usecase.attendance.GetAttendanceSummaryUseCase
import com.studentos.app.domain.usecase.grade.CalculateGradeUseCase
import com.studentos.app.ui.components.*
import com.studentos.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectDetailUiState(
    val subject: Subject? = null,
    val teacher: Teacher? = null,
    val attendanceSummary: AttendanceSummary? = null,
    val gradeSummary: GradeSummary? = null,
    val assignmentCount: Int = 0,
    val completedAssignments: Int = 0,
    val upcomingExams: List<Exam> = emptyList(),
    val noteCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    private val subjectRepo: SubjectRepository,
    private val teacherRepo: TeacherRepository,
    private val attendanceUseCase: GetAttendanceSummaryUseCase,
    private val gradeUseCase: CalculateGradeUseCase,
    private val assignmentRepo: AssignmentRepository,
    private val examRepo: ExamRepository,
    private val noteRepo: NoteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    fun loadSubject(subjectId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val subject = subjectRepo.getById(subjectId)
                val teacher = subject?.teacherId?.let { teacherRepo.getById(it) }
                val attendance = try { attendanceUseCase(subjectId) } catch (_: Exception) { null }
                val grades = try { gradeUseCase(subjectId) } catch (_: Exception) { null }
                val assignments = assignmentRepo.getBySubjectOnce(subjectId)
                val completed = assignments.count { it.status == AssignmentStatus.DONE }
                val exams = examRepo.getBySubjectOnce(subjectId).filter { it.date >= java.time.LocalDate.now() }
                val notes = noteRepo.getAllOnce().count { it.subjectId == subjectId }

                _uiState.value = SubjectDetailUiState(
                    subject = subject,
                    teacher = teacher,
                    attendanceSummary = attendance,
                    gradeSummary = grades,
                    assignmentCount = assignments.size,
                    completedAssignments = completed,
                    upcomingExams = exams,
                    noteCount = notes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: Long,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SubjectDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(subjectId) {
        viewModel.loadSubject(subjectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.subject?.name ?: "Subject", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.subject == null) {
            EmptyState(
                icon = Icons.Outlined.Error,
                title = "Subject not found",
                description = "This subject may have been deleted.",
                modifier = Modifier.padding(padding)
            )
        } else {
            val subject = uiState.subject!!
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Subject header
                item {
                    SubjectHeader(subject)
                }

                // Quick stats
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        val attendance = uiState.attendanceSummary
                        val grades = uiState.gradeSummary
                        if (attendance != null) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Attendance",
                                value = "${attendance.attendancePercentage.toInt()}%",
                                color = when {
                                    attendance.attendancePercentage >= 80 -> AppTheme.extendedColors.success
                                    attendance.attendancePercentage >= 70 -> Warning
                                    else -> Error
                                }
                            )
                        }
                        if (grades != null) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = "Average",
                                value = "${grades.weightedAverage.toInt()}%",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Tasks",
                            value = "${uiState.completedAssignments}/${uiState.assignmentCount}",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Navigation items
                item {
                    SubjectNavItem(
                        icon = Icons.Outlined.Person,
                        title = "Teacher",
                        subtitle = uiState.teacher?.name ?: "Not assigned",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            uiState.teacher?.let {
                                onNavigate(com.studentos.app.ui.navigation.Screen.TeacherDetail.createRoute(it.id))
                            }
                        }
                    )
                }
                item {
                    SubjectNavItem(
                        icon = Icons.Outlined.HowToReg,
                        title = "Attendance",
                        subtitle = uiState.attendanceSummary?.let {
                            "${it.presentCount} present · ${it.absentCount} absent"
                        } ?: "No records",
                        color = AppTheme.extendedColors.success,
                        onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Attendance.createRoute(subjectId)) }
                    )
                }
                item {
                    SubjectNavItem(
                        icon = Icons.Outlined.Grade,
                        title = "Grades",
                        subtitle = uiState.gradeSummary?.let {
                            "${it.grades.size} grades · Average: ${it.weightedAverage.toInt()}%"
                        } ?: "No grades",
                        color = Warning,
                        onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Grades.createRoute(subjectId)) }
                    )
                }
                item {
                    SubjectNavItem(
                        icon = Icons.Outlined.Assignment,
                        title = "Assignments",
                        subtitle = "${uiState.completedAssignments} of ${uiState.assignmentCount} completed",
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Tasks.route) }
                    )
                }
                item {
                    SubjectNavItem(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Schedule",
                        subtitle = "View class schedule",
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Schedule.route) }
                    )
                }
                item {
                    SubjectNavItem(
                        icon = Icons.Outlined.StickyNote2,
                        title = "Notes",
                        subtitle = "${uiState.noteCount} notes",
                        color = Color(0xFFF97316),
                        onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.Notes.route) }
                    )
                }

                // Upcoming exams
                if (uiState.upcomingExams.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Upcoming Exams", modifier = Modifier.padding(top = Spacing.md))
                    }
                    items(uiState.upcomingExams.size) { index ->
                        val exam = uiState.upcomingExams[index]
                        val daysUntil = exam.daysUntil
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ContentPadding.horizontal, vertical = Spacing.xs),
                            onClick = { onNavigate(com.studentos.app.ui.navigation.Screen.ExamEdit.createRoute(exam.id)) }
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exam.type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = exam.date.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusChip(
                                    text = "${daysUntil}d left",
                                    color = if (daysUntil <= 7) Error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectHeader(subject: Subject) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(ContentPadding.horizontal)
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(subject.color).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = subject.shortName.ifEmpty { subject.name.take(2) }.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(subject.color)
            )
        }
        Spacer(modifier = Modifier.width(Spacing.base))
        Column {
            Text(
                text = subject.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subject.type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadius.md),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubjectNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    ListItem(
        headline = title,
        supporting = subtitle,
        leading = { IconBadge(icon = icon, color = color, size = 40.dp) },
        trailing = {
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = Spacing.sm)
    )
}
