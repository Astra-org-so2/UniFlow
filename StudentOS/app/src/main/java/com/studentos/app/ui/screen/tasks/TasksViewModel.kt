package com.studentos.app.ui.screen.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class TaskFilter(val label: String) {
    ALL("All"), TODAY("Today"), TOMORROW("Tomorrow"), THIS_WEEK("This Week"), OVERDUE("Overdue")
}

data class TasksUiState(
    val tasks: List<TaskWithSubject> = emptyList(),
    val filteredTasks: List<TaskWithSubject> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val selectedSubjectId: Long? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

data class TaskWithSubject(
    val assignment: Assignment,
    val subject: Subject?
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val assignmentRepo: AssignmentRepository,
    private val subjectRepo: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val tasks = assignmentRepo.getAllOnce().map { task ->
                    TaskWithSubject(task, subjectRepo.getById(task.subjectId))
                }
                _uiState.value = _uiState.value.copy(
                    tasks = tasks,
                    isLoading = false
                )
                applyFilters()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFilters()
    }

    fun setSubjectFilter(subjectId: Long?) {
        _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId)
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun markComplete(id: Long) {
        viewModelScope.launch {
            assignmentRepo.markComplete(id)
            loadTasks()
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            assignmentRepo.deleteById(id)
            loadTasks()
        }
    }

    fun toggleStatus(id: Long, newStatus: AssignmentStatus) {
        viewModelScope.launch {
            val task = assignmentRepo.getById(id) ?: return@launch
            assignmentRepo.update(task.copy(status = newStatus))
            loadTasks()
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val today = LocalDate.now()
        var filtered = state.tasks

        // Apply status filter
        filtered = when (state.selectedFilter) {
            TaskFilter.ALL -> filtered
            TaskFilter.TODAY -> filtered.filter { it.assignment.deadline == today }
            TaskFilter.TOMORROW -> filtered.filter { it.assignment.deadline == today.plusDays(1) }
            TaskFilter.THIS_WEEK -> filtered.filter {
                it.assignment.deadline != null &&
                it.assignment.deadline >= today &&
                it.assignment.deadline <= today.plusDays(7)
            }
            TaskFilter.OVERDUE -> filtered.filter {
                it.assignment.status != AssignmentStatus.DONE &&
                it.assignment.deadline != null &&
                it.assignment.deadline < today
            }
        }

        // Apply subject filter
        if (state.selectedSubjectId != null) {
            filtered = filtered.filter { it.assignment.subjectId == state.selectedSubjectId }
        }

        // Apply search
        if (state.searchQuery.isNotEmpty()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter {
                it.assignment.title.lowercase().contains(query) ||
                it.assignment.description.lowercase().contains(query) ||
                it.subject?.name?.lowercase()?.contains(query) == true
            }
        }

        // Sort: overdue first, then by deadline, then by priority
        filtered = filtered.sortedWith(
            compareByDescending<TaskWithSubject> { it.assignment.status != AssignmentStatus.DONE }
                .thenBy { it.assignment.deadline ?: LocalDate.MAX }
                .thenByDescending { it.assignment.priority.ordinal }
        )

        _uiState.value = state.copy(filteredTasks = filtered)
    }
}
