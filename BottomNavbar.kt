package np.ict.mad.mad25_t01_team2_npal2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.google.firebase.auth.FirebaseAuth
import np.ict.mad.mad25_t01_team2_npal2.ui.theme.MAD25_T01_Team2_NPAL2Theme

/*class BottomNavbar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MAD25_T01_Team2_NPAL2Theme {
                MAD25_T01_Team2_NPAL2App()
            }
        }
    }
}*/

@PreviewScreenSizes
@Composable
fun MAD25_T01_Team2_NPAL2App() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val firebaseHelper = FirebaseHelper()
    //val currentUserId = "example_user_id" // or get it from FirebaseAuth.currentUser?.uid
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.filter { it.showInNav }.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreenContent(
                    onTaskClick = { currentDestination = AppDestinations.TASKS },
                    onOpenStudentCard = { currentDestination = AppDestinations.STUDENT_CARD },
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.TASKS -> TaskListScreenContent(
                    onCreateTask = { currentDestination = AppDestinations.CREATE_TASKS },
                    firebaseHelper = firebaseHelper,
                    userId = currentUserId,
                    modifier = Modifier.padding(innerPadding))

                AppDestinations.CREATE_TASKS -> CreateTaskScreen(
                    onBack = { currentDestination = AppDestinations.TASKS},
                    firebaseHelper = firebaseHelper,
                    userId = currentUserId
                )
                //AppDestinations.SETTINGS -> ProfileScreen() Add later
                AppDestinations.CALENDAR -> {
                    // Your student calendar screen is finally USED here 🎉
                    StudentCalendarScreen()
                }
                AppDestinations.MAP -> SchoolMap()
                else -> {}
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val showInNav: Boolean = true
) {
    HOME("Home", Icons.Default.Home),
    TASKS("Tasks", Icons.Default.DateRange),
    CREATE_TASKS("Add Task", Icons.Default.AddCircle),
    CALENDAR("Calendar", Icons.Default.DateRange),
    MAP("School Map", Icons.Default.Place),

    STUDENT_CARD("Student Card", Icons.Default.AccountBox, false)

    //SETTINGS("Settings", Icons.Default.Settings),
}

/*@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    MAD25_T01_Team2_NPAL2Theme {
        Greeting2("Android")
    }
}*/