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
        val nextHour = now + 60L * 60L * 1000L

        val todayStart = startOfTodayMillis()
        val tomorrowStart = todayStart + 24L * 60L * 60L * 1000L

        tasks.forEach { task ->
            val startMillis = toMillis(task.date, task.startTime) ?: return@forEach

            if (startMillis !in todayStart until tomorrowStart) return@forEach

            if (startMillis in now..nextHour) {
                NotificationCenter.pushOnce(
                    context = context,
                    firebaseHelper = firebaseHelper,
                    key = "starts-${task.id}-${task.date}-${task.startTime}",
                    userId = userId,
                    title = task.title,
                    message = "${formatTo12Hour(task.startTime)} – ${formatTo12Hour(task.endTime)}",
                    timestamp = startMillis
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
