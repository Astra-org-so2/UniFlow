package com.studentos.app.ui.screen.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import com.studentos.app.domain.usecase.attendance.GetAttendanceSummaryUseCase
import com.studentos.app.domain.usecase.planner.GetStudyPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class TodayUiState(
    val greeting: String = "",
    val nextClass: ScheduleItem? = null,
    val nextClassSubject: Subject? = null,
    val nextClassTeacher: Teacher? = null,
    val todaySchedule: List<TodayScheduleItem> = emptyList(),
    val upcomingTasks: List<Assignment> = emptyList(),
    val upcomingExams: List<Exam> = emptyList(),
    val attendanceWarnings: List<AttendanceWarning> = emptyList(),
    val recommendation: StudyRecommendation? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val scheduleRepo: ScheduleRepository,
    private val subjectRepo: SubjectRepository,
    private val teacherRepo: TeacherRepository,
    private val assignmentRepo: AssignmentRepository,
    private val examRepo: ExamRepository,
    private val attendanceSummaryUseCase: GetAttendanceSummaryUseCase,
    private val studyPlanUseCase: GetStudyPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Greeting
                val student = studentRepo.getCurrentStudentOnce()
                val hour = LocalTime.now().hour
                val greetingPrefix = when {
                    hour < 6 -> "Good night"
                    hour < 12 -> "Good morning"
                    hour < 17 -> "Good afternoon"
                    else -> "Good evening"
                }
                val name = student?.name?.split(" ")?.firstOrNull() ?: "Student"
                val greeting = "$greetingPrefix, $name"

                // Today's schedule
                val today = LocalDate.now()
                val scheduleItems = scheduleRepo.getForDateOnce(today)
                val scheduleWithDetails = scheduleItems.map { item ->
                    val subject = subjectRepo.getById(item.subjectId)
                    val teacher = item.teacherId?.let { teacherRepo.getById(it) }
                    val now = LocalTime.now()
                    val isNow = now.isAfter(item.startTime) && now.isBefore(item.endTime)
                    TodayScheduleItem(
                        scheduleItem = item,
                        subject = subject ?: Subject(id = 0, name = "Unknown"),
                        teacher = teacher,
                        isNow = isNow
                    )
                }.sortedBy { it.scheduleItem.startTime }

                // Find next class
                val now = LocalTime.now()
                val nextClassItem = scheduleWithDetails.firstOrNull {
                    it.scheduleItem.startTime.isAfter(now)
                }

                // Upcoming tasks (next 7 days)
                val endDate = today.plusDays(7)
                val tasks = assignmentRepo.getInRangeOnce(today, endDate)
                    .filter { it.status != AssignmentStatus.DONE }
                    .sortedBy { it.deadline }
                    .take(5)

                // Upcoming exams
                val exams = examRepo.getUpcomingOnce().take(3)

                // Attendance warnings
                val subjects = subjectRepo.getAllOnce()
                val warnings = mutableListOf<AttendanceWarning>()
                for (subject in subjects) {
                    try {
                        val summary = attendanceSummaryUseCase(subject.id)
                        if (summary.totalClasses > 0 && summary.attendancePercentage < 80.0) {
                            warnings.add(
                                AttendanceWarning(
                                    subjectId = subject.id,
                                    subjectName = subject.name,
                                    percentage = summary.attendancePercentage,
                                    safeAbsencesRemaining = summary.safeAbsencesRemaining
                                )
                            )
                        }
                    } catch (_: Exception) { }
                }

                // Smart recommendation
                val plan = studyPlanUseCase()
                val recommendation = plan.recommendations.firstOrNull()

                _uiState.value = TodayUiState(
                    greeting = greeting,
                    nextClass = nextClassItem?.scheduleItem,
                    nextClassSubject = nextClassItem?.subject,
                    nextClassTeacher = nextClassItem?.teacher,
                    todaySchedule = scheduleWithDetails,
                    upcomingTasks = tasks,
                    upcomingExams = exams,
                    attendanceWarnings = warnings,
                    recommendation = recommendation,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }

    fun refresh() = loadDashboard()
}
