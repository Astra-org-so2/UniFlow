package com.studentos.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.studentos.app.MainActivity

object TasksWidgetKeys {
    val overdueCount = intPreferencesKey("overdue_count")
    val todayCount = intPreferencesKey("today_count")
    val totalCount = intPreferencesKey("total_count")
    val overdueText = stringPreferencesKey("overdue_text")
    val todayText = stringPreferencesKey("today_text")
}

class TasksWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        val overdue = prefs[TasksWidgetKeys.overdueCount] ?: 0
        val today = prefs[TasksWidgetKeys.todayCount] ?: 0
        val total = prefs[TasksWidgetKeys.totalCount] ?: 0

        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(GlanceTheme.colors.surface)
                    .cornerRadius(16.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    Text(
                        text = "TASKS",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(Color(0xFF6B7280)),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (overdue > 0) {
                            Text(
                                text = "$overdue overdue",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = ColorProvider(Color(0xFFEF4444)),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(16.dp))
                        }

                        Text(
                            text = "$today today",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = GlanceTheme.colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "$total active tasks",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color(0xFF9CA3AF))
                        )
                    )
                }
            }
        }
    }
}

class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TasksWidget()
}
