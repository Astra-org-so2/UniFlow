package com.studentos.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.studentos.app.MainActivity
import com.studentos.app.R

object NextClassWidgetKeys {
    val subjectName = stringPreferencesKey("subject_name")
    val startTime = stringPreferencesKey("start_time")
    val room = stringPreferencesKey("room")
    val teacher = stringPreferencesKey("teacher")
    val timeUntil = stringPreferencesKey("time_until")
}

class NextClassWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        val subjectName = prefs[NextClassWidgetKeys.subjectName]
        val startTime = prefs[NextClassWidgetKeys.startTime]
        val room = prefs[NextClassWidgetKeys.room]
        val teacher = prefs[NextClassWidgetKeys.teacher]
        val timeUntil = prefs[NextClassWidgetKeys.timeUntil]

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
                        text = "NEXT CLASS",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(Color(0xFF6B7280)),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (subjectName != null) {
                        Text(
                            text = subjectName,
                            style = TextStyle(
                                fontSize = 18.sp,
                                color = GlanceTheme.colors.onSurface,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            if (startTime != null) {
                                Text(
                                    text = startTime,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = GlanceTheme.colors.onSurface
                                    )
                                )
                            }
                            if (room != null && room.isNotEmpty()) {
                                Spacer(modifier = GlanceModifier.width(12.dp))
                                Text(
                                    text = "Room $room",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = ColorProvider(Color(0xFF6B7280))
                                    )
                                )
                            }
                        }

                        if (teacher != null && teacher.isNotEmpty()) {
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = teacher,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF9CA3AF))
                                )
                            )
                        }

                        if (timeUntil != null) {
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            Text(
                                text = timeUntil,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF4F46E5)),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    } else {
                        Spacer(modifier = GlanceModifier.height(16.dp))
                        Text(
                            text = "No upcoming classes",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ColorProvider(Color(0xFF9CA3AF))
                            )
                        )
                    }
                }
            }
        }
    }
}

class NextClassWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = NextClassWidget()
}
