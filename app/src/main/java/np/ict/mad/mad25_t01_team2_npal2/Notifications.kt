package np.ict.mad.mad25_t01_team2_npal2

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


data class InAppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

fun todayYMD(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

object NotificationCenter {

    private val _notifications =
        MutableStateFlow<List<InAppNotification>>(emptyList())

    val notifications: StateFlow<List<InAppNotification>> = _notifications

    private val pushedKeys = mutableSetOf<String>()

    fun push(context: Context, userId: String, title: String, message: String) {
        if (!isNotificationsEnabled(context)) return

        val notification = InAppNotification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            message = message
        )
        _notifications.value = listOf(notification) + _notifications.value
    }

    fun pushOnce(context: Context, key: String, userId: String, title: String, message: String) {
        if (pushedKeys.contains(key)) return
        pushedKeys.add(key)
        push(context, userId, title, message)
    }

    fun markAllRead(userId: String) {
        _notifications.value = _notifications.value.map {
            if (it.userId == userId) it.copy(isRead = true) else it
        }
    }

    fun unreadCount(userId: String): Int {
        return _notifications.value.count {
            it.userId == userId && !it.isRead
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    userId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val all by NotificationCenter.notifications.collectAsState()
    val notifications = all.filter { it.userId == userId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { NotificationCenter.markAllRead(userId) }) {
                        Text("Mark all read")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            if (notifications.isEmpty()) {
                Text(
                    text = "No notifications",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications) { notification ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    if (notification.isRead)
                                        MaterialTheme.colorScheme.surface
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = notification.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = notification.message,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun isNotificationsEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    return prefs.getBoolean("notifications_enabled", true)
}

fun setNotificationsEnabled(context: Context, enabled: Boolean) {
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("notifications_enabled", enabled).apply()
}

