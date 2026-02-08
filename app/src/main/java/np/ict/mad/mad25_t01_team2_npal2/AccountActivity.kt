package np.ict.mad.mad25_t01_team2_npal2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AccountActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firebaseHelper = FirebaseHelper() // or pass it if needed

        setContent {
            MaterialTheme {
                // Just use your existing AccountScreen composable
                AccountScreen(
                    onBack = { finish() }, // finish the activity on back
                    firebaseHelper = firebaseHelper,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}