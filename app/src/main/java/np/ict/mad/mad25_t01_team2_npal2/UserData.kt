package np.ict.mad.mad25_t01_team2_npal2

import java.io.Serializable

data class UserData(
    val username: String = "",
    val email: String = "",
    val studentId: String = "",
    val uid: String = "",
    val cardThemeIndex: Int = 0
) : Serializable