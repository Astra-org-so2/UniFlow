package com.studentos.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    // Main tabs
    data object Today : Screen("today")
    data object Schedule : Screen("schedule")
    data object Tasks : Screen("tasks")
    data object Study : Screen("study")
    data object Analytics : Screen("analytics")

    // Today sub-screens
    data object TodayDetail : Screen("today_detail/{itemId}") {
        fun createRoute(itemId: Long) = "today_detail/$itemId"
    }

    // Schedule sub-screens
    data object ScheduleAdd : Screen("schedule_add")
    data object ScheduleEdit : Screen("schedule_edit/{id}") {
        fun createRoute(id: Long) = "schedule_edit/$id"
    }

    // Tasks sub-screens
    data object TaskAdd : Screen("task_add")
    data object TaskEdit : Screen("task_edit/{id}") {
        fun createRoute(id: Long) = "task_edit/$id"
    }
    data object TaskDetail : Screen("task_detail/{id}") {
        fun createRoute(id: Long) = "task_detail/$id"
    }

    // Study sub-screens
    data object Subjects : Screen("subjects")
    data object SubjectDetail : Screen("subject_detail/{id}") {
        fun createRoute(id: Long) = "subject_detail/$id"
    }
    data object SubjectAdd : Screen("subject_add")
    data object SubjectEdit : Screen("subject_edit/{id}") {
        fun createRoute(id: Long) = "subject_edit/$id"
    }
    data object Teachers : Screen("teachers")
    data object TeacherDetail : Screen("teacher_detail/{id}") {
        fun createRoute(id: Long) = "teacher_detail/$id"
    }
    data object TeacherAdd : Screen("teacher_add")
    data object TeacherEdit : Screen("teacher_edit/{id}") {
        fun createRoute(id: Long) = "teacher_edit/$id"
    }
    data object Grades : Screen("grades/{subjectId}") {
        fun createRoute(subjectId: Long) = "grades/$subjectId"
    }
    data object GradeAdd : Screen("grade_add/{subjectId}") {
        fun createRoute(subjectId: Long) = "grade_add/$subjectId"
    }
    data object Attendance : Screen("attendance/{subjectId}") {
        fun createRoute(subjectId: Long) = "attendance/$subjectId"
    }
    data object AttendanceAdd : Screen("attendance_add/{subjectId}") {
        fun createRoute(subjectId: Long) = "attendance_add/$subjectId"
    }
    data object Exams : Screen("exams")
    data object ExamAdd : Screen("exam_add")
    data object ExamEdit : Screen("exam_edit/{id}") {
        fun createRoute(id: Long) = "exam_edit/$id"
    }
    data object Notes : Screen("notes")
    data object NoteDetail : Screen("note_detail/{id}") {
        fun createRoute(id: Long) = "note_detail/$id"
    }
    data object NoteAdd : Screen("note_add")
    data object NoteEdit : Screen("note_edit/{id}") {
        fun createRoute(id: Long) = "note_edit/$id"
    }

    // Analytics sub-screens
    data object SubjectAnalytics : Screen("subject_analytics/{subjectId}") {
        fun createRoute(subjectId: Long) = "subject_analytics/$subjectId"
    }

    // Settings
    data object Settings : Screen("settings")
    data object Onboarding : Screen("onboarding")

    // AI
    data object AIChat : Screen("ai_chat")

    // Backup
    data object Backup : Screen("backup")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Today, "Today", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Schedule, "Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomNavItem(Screen.Tasks, "Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    BottomNavItem(Screen.Study, "Study", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    BottomNavItem(Screen.Analytics, "Stats", Icons.Filled.Analytics, Icons.Outlined.Analytics),
)
