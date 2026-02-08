package np.ict.mad.mad25_t01_team2_npal2

import android.content.Context

object PrefsHelper {
    private const val PREFS_NAME = "npal2_prefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"

    fun saveLogin(context: Context, username: String, password: String? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            password?.let { putString(KEY_PASSWORD, it) } // optional
            apply()
        }
    }

    fun getSavedUsername(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getSavedPassword(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, null)
    }

    fun clearLogin(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
