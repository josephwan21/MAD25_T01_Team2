package np.ict.mad.mad25_t01_team2_npal2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class AccountActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firebaseHelper = FirebaseHelper()

        setContent {
            val context = LocalContext.current
            val isDarkMode = remember {
                mutableStateOf(ThemePrefs.loadDarkMode(context))
            }
            val colors = if (isDarkMode.value) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }
            MaterialTheme(
                colorScheme = colors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AccountScreen(
                        onBack = { finish() },
                        firebaseHelper = firebaseHelper,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}