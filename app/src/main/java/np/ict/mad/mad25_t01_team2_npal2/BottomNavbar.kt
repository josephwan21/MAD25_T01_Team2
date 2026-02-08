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
import java.util.Calendar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import np.ict.mad.mad25_t01_team2_npal2.ui.theme.MAD25_T01_Team2_NPAL2Theme
import java.text.SimpleDateFormat

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

@Composable
fun MAD25_T01_Team2_NPAL2App(
    onLogout : () -> Unit
) {

    val context = LocalContext.current
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val firebaseHelper = FirebaseHelper()
    //val currentUserId = "example_user_id" // or get it from FirebaseAuth.currentUser?.uid
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var settingsSubScreen by rememberSaveable { mutableStateOf<SettingsSubScreen?>(null) }



    // ✅ THIS is what makes notifications actually get pushed
    LaunchedEffect(currentUserId) {
        if (currentUserId.isBlank()) return@LaunchedEffect

        while (true) {
            try {
                val tasks = firebaseHelper.getTasks(currentUserId)
                TaskReminder.run(
                    context = context,
                    firebaseHelper = firebaseHelper,
                    userId = currentUserId,
                    tasks = tasks
                )
            } catch (e: Exception) {
                android.util.Log.e("TaskReminder", "run failed", e)
            }
            delay(60_000) // check every minute
        }
    }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = {
                        showNotifications = false // close notif screen if open
                        settingsSubScreen = null
                        currentDestination = it
                    }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->             // If user opened notifications (via bell), show it FULL SCREEN
            settingsSubScreen?.let { subScreen ->
                when (subScreen) {
                    SettingsSubScreen.NOTIFICATIONS -> NotificationSettingsScreen(onBack = { settingsSubScreen = null }, modifier = Modifier)
                    SettingsSubScreen.ACCOUNT -> AccountScreen(onBack = { settingsSubScreen = null }, firebaseHelper, modifier = Modifier)
                    SettingsSubScreen.SUPPORT -> HelpSupportScreen(onBack = { settingsSubScreen = null }, modifier = Modifier)
                }
                return@Scaffold
            }

            if (showNotifications) {
                NotificationsScreen(
                    firebaseHelper = firebaseHelper,
                    userId = currentUserId,
                    onBack = { showNotifications = false }
                )
                return@Scaffold
            }

            val contentModifier = Modifier.padding(innerPadding)
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreenContent(
                    onTaskClick = { currentDestination = AppDestinations.TASKS },
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.TASKS -> TaskListScreenContent(
                    firebaseHelper = firebaseHelper,
                    userId = currentUserId,
                    onCreateTask = { currentDestination = AppDestinations.CREATE_TASKS },
                    onOpenNotifications = { showNotifications = true },
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.CREATE_TASKS -> CreateTaskScreen(
                    onBack = { currentDestination = AppDestinations.TASKS},
                    firebaseHelper = firebaseHelper,
                    userId = currentUserId
                )
                //AppDestinations.SETTINGS -> ProfileScreen() Add later
                AppDestinations.CALENDAR -> {
                    StudentCalendarScreen(
                        firebaseHelper = firebaseHelper,
                        userId = currentUserId
                    )
                }
                AppDestinations.MAP -> SchoolMap()
                AppDestinations.SETTINGS -> SettingsScreen(
                    onLogout = onLogout,
                    onAccountClick = { settingsSubScreen = SettingsSubScreen.ACCOUNT },
                    onNotificationsClick = { settingsSubScreen = SettingsSubScreen.NOTIFICATIONS },
                    onHelpClick = { settingsSubScreen = SettingsSubScreen.SUPPORT},
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    TASKS("Tasks", Icons.Default.DateRange),
    CREATE_TASKS("Add Task", Icons.Default.AddCircle),
    CALENDAR("Calendar", Icons.Default.DateRange),
    MAP("School Map", Icons.Default.Place),
    SETTINGS("Settings", Icons.Default.Settings)
}

enum class SettingsSubScreen {
    NOTIFICATIONS,
    ACCOUNT,

    SUPPORT
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

