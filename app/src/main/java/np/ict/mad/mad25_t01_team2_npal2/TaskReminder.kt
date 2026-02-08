package np.ict.mad.mad25_t01_team2_npal2

import android.content.Context
import java.util.Calendar
import java.util.Locale

object TaskReminder {

    fun run(
        context: Context,
        firebaseHelper: FirebaseHelper,
        userId: String,
        tasks: List<Task>
    ) {
        val now = System.currentTimeMillis()
        val inOneHour = now + 60L * 60L * 1000L

        val todayStart = startOfTodayMillis()
        val tomorrowStart = todayStart + 24L * 60L * 60L * 1000L

        tasks.forEach { task ->
            val startMillis = toMillis(task.date, task.startTime) ?: return@forEach

            // Only today tasks
            if (startMillis !in todayStart until tomorrowStart) return@forEach

            // Past tasks don't need reminders
            if (startMillis <= now) return@forEach

            val timeRange = "${formatTo12Hour(task.startTime)} – ${formatTo12Hour(task.endTime)}"

            if (startMillis <= inOneHour) {
                NotificationCenter.pushOnce(
                    context = context,
                    firebaseHelper = firebaseHelper,
                    // stable key (don’t rely only on task.id)
                    key = "starts1h-${task.date}-${task.startTime}-${task.endTime}-${task.title}",
                    userId = userId,
                    title = task.title,
                    // store message WITHOUT duplicating label in UI if you want
                    message = "Starts in 1 hour • $timeRange",
                    timestamp = now,
                    taskCategory = task.category
                )
            } else {
                // Optional: keep this if you want “Today” reminders for all tasks
                NotificationCenter.pushOnce(
                    context = context,
                    firebaseHelper = firebaseHelper,
                    key = "today-${task.date}-${task.startTime}-${task.endTime}-${task.title}",
                    userId = userId,
                    title = task.title,
                    message = timeRange,
                    timestamp = now,
                    taskCategory = task.category
                )
            }
        }
    }

    fun toMillis(date: String, time: String): Long? {
        return try {
            val d = date.split("-").map { it.toInt() }
            val t = time.split(":").map { it.toInt() }

            Calendar.getInstance(Locale.getDefault()).apply {
                set(Calendar.YEAR, d[0])
                set(Calendar.MONTH, d[1] - 1)
                set(Calendar.DAY_OF_MONTH, d[2])
                set(Calendar.HOUR_OF_DAY, t[0])
                set(Calendar.MINUTE, t[1])
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (_: Exception) {
            null
        }
    }

    private fun startOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
