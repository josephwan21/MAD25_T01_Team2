package np.ict.mad.mad25_t01_team2_npal2

import android.content.Context

fun isNotificationsEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    return prefs.getBoolean("notifications_enabled", true)
}

fun setNotificationsEnabled(context: Context, enabled: Boolean) {
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("notifications_enabled", enabled).apply()
}
