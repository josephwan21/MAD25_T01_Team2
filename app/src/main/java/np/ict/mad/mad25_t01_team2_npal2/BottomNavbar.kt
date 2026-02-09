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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MAD25_T01_Team2_NPAL2App(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var isDarkMode by rememberSaveable {
        mutableStateOf(ThemePrefs.loadDarkMode(context))
    }

    val colorScheme = if (isDarkMode) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val firebaseHelper = FirebaseHelper()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""

    // Emily: User Data State
    var user by remember { mutableStateOf(UserData(uid = currentUserId,)) }

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

        // Emily: fetch user profile from Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                user = UserData(
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: (currentUser?.email ?: ""),
                    studentId = doc.getString("studentId") ?: "",
                    uid = doc.getString("uid") ?: currentUserId,
                    cardThemeIndex = (doc.getLong("cardThemeIndex") ?: 0L).toInt()                )
            }
            .addOnFailureListener { e ->
                android.util.Log.e("UserData", "Failed to load user profile", e)
            }
    }

    MaterialTheme(colorScheme = colorScheme) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries
                    .filter { it.showInNav } // hides Student Card from navbar
                    .forEach { dest ->
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

                // Settings Sub-Screens
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

                // Full-Screen Notifications Screen
                if (showNotifications) {
                    NotificationsScreen(
                        firebaseHelper = firebaseHelper,
                        userId = currentUserId,
                        onBack = { showNotifications = false }
                    )
                    return@Scaffold
                }

                // Navigation Content
                when (currentDestination) {
                    AppDestinations.HOME ->
                        HomeScreenContent(
                            user = user,
                            firebaseHelper = firebaseHelper,
                            onOpenStudentCard = { currentDestination = AppDestinations.STUDENT_CARD },
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

                    AppDestinations.STUDENT_CARD ->
                        StudentCardScreen(
                            user = user,
                            onUserUpdated = { updatedUser -> user = updatedUser },
                            onBack = { currentDestination = AppDestinations.HOME }
                        )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val showInNav: Boolean = true
) {
    // Visible Navigation Links
    HOME("Home", Icons.Default.Home),
    TASKS("Tasks", Icons.Default.DateRange),
    CREATE_TASKS("Add Task", Icons.Default.AddCircle),
    CALENDAR("Calendar", Icons.Default.DateRange),
    MAP(" Map", Icons.Default.Place),
    SETTINGS("Settings", Icons.Default.Settings),

    // Hidden Navigation Links
    STUDENT_CARD("Student Card", Icons.Default.AddCircle, false)
}

enum class SettingsSubScreen {
    NOTIFICATIONS,
    ACCOUNT,
    SUPPORT
}