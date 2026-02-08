package np.ict.mad.mad25_t01_team2_npal2

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import np.ict.mad.mad25_t01_team2_npal2.ui.theme.MAD25_T01_Team2_NPAL2Theme

class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MAD25_T01_Team2_NPAL2Theme {
                AppContent()
                /*Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }*/
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AppContent() {
    var loggedIn by rememberSaveable { mutableStateOf(false) }
    var showRegister by rememberSaveable { mutableStateOf(false) }

    if (loggedIn) {
        MAD25_T01_Team2_NPAL2App(
            onLogout = { loggedIn = false }
        )
    } else {
        if (showRegister) {
            RegisterScreen(
                onRegisterSuccess = { showRegister = false },
                onBack = { showRegister = false }
            )
        } else {
            LoginScreen(
                onLoginSuccess = { loggedIn = true },
                onRegisterClick = { showRegister = true}
            )
        }
    }
}