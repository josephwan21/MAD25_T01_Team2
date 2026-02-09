package np.ict.mad.mad25_t01_team2_npal2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun MAD25_T01_Team2_NPAL2App(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var isDarkMode by rememberSaveable {
        mutableStateOf(ThemePrefs.loadDarkMode(context))
    }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val firebaseHelper = FirebaseHelper()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var settingsSubScreen by rememberSaveable { mutableStateOf<SettingsSubScreen?>(null) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isBlank()) return@LaunchedEffect

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
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { dest ->
                item(
                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                    label = { Text(dest.label) },
                    selected = dest == currentDestination,
                    onClick = {
                        showNotifications = false
                        settingsSubScreen = null
                        currentDestination = dest
                    }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

            //  Settings subscreens
            settingsSubScreen?.let { subScreen ->
                when (subScreen) {
                    SettingsSubScreen.NOTIFICATIONS ->
                        NotificationSettingsScreen(
                            onBack = { settingsSubScreen = null },
                            modifier = Modifier
                        )

                    SettingsSubScreen.ACCOUNT ->
                        AccountScreen(
                            onBack = { settingsSubScreen = null },
                            firebaseHelper = firebaseHelper,
                            modifier = Modifier
                        )

                    SettingsSubScreen.SUPPORT ->
                        HelpSupportScreen(
                            onBack = { settingsSubScreen = null },
                            modifier = Modifier
                        )
                }
                return@Scaffold
            }

            //  Fullscreen notifications screen
            if (showNotifications) {
                NotificationsScreen(
                    firebaseHelper = firebaseHelper,
                    userId = currentUserId,
                    onBack = { showNotifications = false }
                )
                return@Scaffold
            }

            // Main navigation content
            when (currentDestination) {
                AppDestinations.HOME ->
                    HomeScreenContent(
                        onTaskClick = { currentDestination = AppDestinations.TASKS },
                        modifier = Modifier.padding(innerPadding)
                    )

                AppDestinations.TASKS ->
                    TaskListScreenContent(
                        firebaseHelper = firebaseHelper,
                        userId = currentUserId,
                        onCreateTask = { currentDestination = AppDestinations.CREATE_TASKS },
                        onOpenNotifications = { showNotifications = true },
                        modifier = Modifier.padding(innerPadding)
                    )

                AppDestinations.CREATE_TASKS ->
                    CreateTaskScreen(
                        onBack = { currentDestination = AppDestinations.TASKS },
                        firebaseHelper = firebaseHelper,
                        userId = currentUserId
                    )

                AppDestinations.CALENDAR ->
                    StudentCalendarScreen(
                        firebaseHelper = firebaseHelper,
                        userId = currentUserId
                    )

                AppDestinations.MAP -> SchoolMap()

                AppDestinations.SETTINGS ->
                    SettingsScreen(
                        onLogout = onLogout,
                        onAccountClick = { settingsSubScreen = SettingsSubScreen.ACCOUNT },
                        onNotificationsClick = { settingsSubScreen = SettingsSubScreen.NOTIFICATIONS },
                        onHelpClick = { settingsSubScreen = SettingsSubScreen.SUPPORT },
                        isDarkMode = isDarkMode,
                        onDarkModeToggle = {
                            isDarkMode = it
                            ThemePrefs.saveDarkMode(context, it)
                        },
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
