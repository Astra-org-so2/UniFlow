package com.studentos.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.studentos.app.MainActivity
import com.studentos.app.domain.model.*
import com.studentos.app.domain.repository.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

object NotificationChannels {
    const val CLASS_REMINDER = "class_reminder"
    const val DEADLINE_ALERT = "deadline_alert"
    const val EXAM_REMINDER = "exam_reminder"
    const val ATTENDANCE_WARNING = "attendance_warning"
}

object NotificationIds {
    const val CLASS_BASE = 1000
    const val DEADLINE_BASE = 2000
    const val EXAM_BASE = 3000
    const val ATTENDANCE_BASE = 4000
}

fun createNotificationChannels(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channels = listOf(
        NotificationChannel(
            NotificationChannels.CLASS_REMINDER,
            "Class Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Reminders before classes start" },

        NotificationChannel(
            NotificationChannels.DEADLINE_ALERT,
            "Deadline Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Alerts for approaching deadlines" },

        NotificationChannel(
            NotificationChannels.EXAM_REMINDER,
            "Exam Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Reminders for upcoming exams" },

        NotificationChannel(
            NotificationChannels.ATTENDANCE_WARNING,
            "Attendance Warnings",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Warnings about low attendance" }
    )

    manager.createNotificationChannels(channels)
}

// ── Class Reminder Worker ──────────────────────────────────────────────────

@AndroidEntryPoint
class ClassReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scheduleRepo: ScheduleRepository,
    private val subjectRepo: SubjectRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val scheduleId = inputData.getLong("schedule_id", -1)
        val subjectId = inputData.getLong("subject_id", -1)
        val room = inputData.getString("room") ?: ""
        val startTime = inputData.getString("start_time") ?: return Result.failure()
        val subjectName = subjectRepo.getById(subjectId)?.name ?: "Class"

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.CLASS_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming: $subjectName")
            .setContentText("Starts at $startTime${if (room.isNotEmpty()) " · Room $room" else ""}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationIds.CLASS_BASE + scheduleId.toInt(), notification)

        return Result.success()
    }
}

// ── Deadline Reminder Worker ───────────────────────────────────────────────

@AndroidEntryPoint
class DeadlineReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val assignmentRepo: AssignmentRepository,
    private val subjectRepo: SubjectRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val assignmentId = inputData.getLong("assignment_id", -1)
        val assignment = assignmentRepo.getById(assignmentId) ?: return Result.failure()
        if (assignment.status == AssignmentStatus.DONE) return Result.success()

        val subjectName = subjectRepo.getById(assignment.subjectId)?.name ?: ""
        val dueText = assignment.deadline?.let {
            val days = ChronoUnit.DAYS.between(LocalDate.now(), it)
            when {
                days < 0 -> "Overdue"
                days == 0L -> "Due today"
                days == 1L -> "Due tomorrow"
                else -> "Due in ${days}d"
            }
        } ?: ""

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.DEADLINE_ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Due soon: ${assignment.title}")
            .setContentText("$dueText${if (subjectName.isNotEmpty()) " · $subjectName" else ""}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationIds.DEADLINE_BASE + assignmentId.toInt(), notification)

        return Result.success()
    }
}

// ── Exam Reminder Worker ───────────────────────────────────────────────────

@AndroidEntryPoint
class ExamReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val examRepo: ExamRepository,
    private val subjectRepo: SubjectRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val examId = inputData.getLong("exam_id", -1)
        val exam = examRepo.getById(examId) ?: return Result.failure()
        val subjectName = subjectRepo.getById(exam.subjectId)?.name ?: "Exam"
        val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), exam.date)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.EXAM_REMINDER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Exam in ${daysUntil}d: $subjectName")
            .setContentText("${exam.type.name.replace("_", " ")} · ${exam.date}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationIds.EXAM_BASE + examId.toInt(), notification)

        return Result.success()
    }
}

// ── Boot Receiver ──────────────────────────────────────────────────────────

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduleRepo: ScheduleRepository
    @Inject lateinit var assignmentRepo: AssignmentRepository
    @Inject lateinit var examRepo: ExamRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            createNotificationChannels(context)
            // Reschedule notifications would go here
            // using WorkManager to schedule reminders for upcoming events
        }
    }
}
