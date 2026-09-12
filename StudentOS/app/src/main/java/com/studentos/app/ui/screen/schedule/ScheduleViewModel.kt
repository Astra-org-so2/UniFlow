package com.studentos.app.ui.screen.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class ScheduleUiState(
    val selectedDay: Int = LocalDate.now().dayOfWeek.value,
    val selectedView: Int = 0, // 0=Day, 1=Week, 2=Month
    val weekSchedule: Map<Int, List<ScheduleItemWithDetails>> = emptyMap(),
    val daySchedule: List<ScheduleItemWithDetails> = emptyList(),
    val currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ScheduleItemWithDetails(
    val item: ScheduleItem,
    val subject: Subject,
    val teacher: Teacher?
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepo: ScheduleRepository,
    private val subjectRepo: SubjectRepository,
    private val teacherRepo: TeacherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val allItems = scheduleRepo.getAllOnce()
                val today = LocalDate.now()
                val weekStart = today.with(DayOfWeek.MONDAY)

                // Build week map
                val weekMap = mutableMapOf<Int, List<ScheduleItemWithDetails>>()
                for (day in 1..7) {
                    val items = allItems
                        .filter { it.dayOfWeek.value == day }
                        .filter { today.isAfter(it.semesterStart.minusDays(1)) && today.isBefore(it.semesterEnd.plusDays(1)) }
                        .sortedBy { it.startTime }
                        .map { item ->
                            val subject = subjectRepo.getById(item.subjectId)
                            val teacher = item.teacherId?.let { teacherRepo.getById(it) }
                            ScheduleItemWithDetails(item, subject ?: Subject(id = 0, name = "Unknown"), teacher)
                        }
                    weekMap[day] = items
                }

                // Day schedule
                val daySchedule = weekMap[today.dayOfWeek.value] ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    weekSchedule = weekMap,
                    daySchedule = daySchedule,
                    currentWeekStart = weekStart,
                    selectedDay = today.dayOfWeek.value,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectDay(day: Int) {
        val daySchedule = _uiState.value.weekSchedule[day] ?: emptyList()
        _uiState.value = _uiState.value.copy(selectedDay = day, daySchedule = daySchedule)
    }

    fun setView(view: Int) {
        _uiState.value = _uiState.value.copy(selectedView = view)
    }

    fun deleteScheduleItem(id: Long) {
        viewModelScope.launch {
            scheduleRepo.deleteById(id)
            loadSchedule()
        }
    }

    fun duplicateScheduleItem(id: Long) {
        viewModelScope.launch {
            scheduleRepo.duplicate(id)
            loadSchedule()
        }
    }
}
