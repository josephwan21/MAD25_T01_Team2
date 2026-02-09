package np.ict.mad.mad25_t01_team2_npal2

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudentCardScreen(
    user: UserData,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Student Card",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Firestore User Data
        Text("UID: ${user.uid}")
        Text("Username: ${user.username}")
        Text("Email: ${user.email}")
        Text("Student ID: ${user.studentId}")

        Spacer(modifier = Modifier.height(12.dp))
    }
}