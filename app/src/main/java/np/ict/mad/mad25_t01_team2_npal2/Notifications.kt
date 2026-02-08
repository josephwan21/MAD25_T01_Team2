package np.ict.mad.mad25_t01_team2_npal2

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

/* -------------------- DATA -------------------- */

enum class NotificationKind {
    GENERIC,
    OVERDUE
}

data class InAppNotification(
    val id: String = "",
    val key: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val kind: NotificationKind = NotificationKind.GENERIC,
    val taskCategory: String? = null
)


/* -------------------- CENTER -------------------- */

object NotificationCenter {

    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications

    private val pushedKeys = mutableSetOf<String>()

    fun setAll(list: List<InAppNotification>) {
        _notifications.value = list
        pushedKeys.clear()
        pushedKeys.addAll(list.mapNotNull { it.key.ifBlank { null } })
    }

    fun clearAll(userId: String) {
        _notifications.value = _notifications.value.filterNot { it.userId == userId }
        pushedKeys.clear()
        pushedKeys.addAll(_notifications.value.mapNotNull { it.key.ifBlank { null } })
    }


    fun pushOnce(

        context: Context,
        firebaseHelper: FirebaseHelper,
        key: String,
        userId: String,
        title: String,
        message: String,
        timestamp: Long = System.currentTimeMillis(),
        taskCategory: String? = null

    ) {
        if (!isNotificationsEnabled(context)) return
        if (pushedKeys.contains(key)) return
        pushedKeys.add(key)

        val n = InAppNotification(
            id = key,
            key = key,
            userId = userId,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = false,
            taskCategory = taskCategory
        )




        if (_notifications.value.any { it.id == key }) return

        _notifications.value = listOf(n) + _notifications.value

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            firebaseHelper.saveNotificationIfMissing(userId, n)
        }
    }

    fun markRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllRead(userId: String) {
        _notifications.value = _notifications.value.map {
            if (it.userId == userId) it.copy(isRead = true) else it
        }
    }

    fun unreadCount(userId: String): Int {
        return _notifications.value.count { it.userId == userId && !it.isRead }
    }
}

/* -------------------- HELPERS -------------------- */

fun startOfTodayMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun timeAgo(timestamp: Long): String {
    val diff = (System.currentTimeMillis() - timestamp).coerceAtLeast(0)

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

fun cleanStartsMessage(msg: String): String {
    return msg
        .removePrefix("Starts in 1 hour • ")
        .removePrefix("Starts in 1 hour · ")
        .removePrefix("Starts in 1 hour - ")
        .removePrefix("Starts in 1 hour ")
        .trim()
}


/* -------------------- SCREEN -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    firebaseHelper: FirebaseHelper,
    userId: String,
    onBack: () -> Unit
) {
    val all by NotificationCenter.notifications.collectAsState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(userId) {
        val remote = firebaseHelper.getNotifications(userId)
        NotificationCenter.setAll(remote)
    }

    val todayStart = startOfTodayMillis()
    val yesterdayStart = todayStart - 24L * 60L * 60L * 1000L

    val userNotifications = all
        .filter { it.userId == userId }
        .sortedByDescending { it.timestamp }

    val todayList = userNotifications.filter { it.timestamp >= todayStart }
    val yesterdayList = userNotifications.filter { it.timestamp in yesterdayStart until todayStart }
    val earlierList = userNotifications.filter { it.timestamp < yesterdayStart }
    var showEarlier by rememberSaveable { mutableStateOf(false) }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        NotificationCenter.markAllRead(userId)
                        scope.launch { firebaseHelper.markAllNotificationsRead(userId) }
                    }) { Text("Mark all read") }

                    TextButton(onClick = {
                        scope.launch {
                            val ok = firebaseHelper.deleteAllNotifications(userId)
                            if (ok) NotificationCenter.clearAll(userId)
                        }
                    }) { Text("Clear") }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            if (userNotifications.isEmpty()) {
                Text("No notifications")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    if (todayList.isNotEmpty()) {
                        item {
                            Text("Today", style = MaterialTheme.typography.labelLarge)
                        }
                        items(todayList, key = { it.id }) { n ->
                            NotificationBubble(n, showStartsLabel = true)
                        }

                    }

                    if (yesterdayList.isNotEmpty()) {
                        item {
                            Text("Yesterday", style = MaterialTheme.typography.labelLarge)
                        }
                        items(yesterdayList, key = { it.id }) { n ->
                            NotificationBubble(n, showStartsLabel = false)
                        }

                    }

                    if (earlierList.isNotEmpty()) {

                        // Header row with arrow
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEarlier = !showEarlier }
                                    .padding(top = 8.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Earlier", style = MaterialTheme.typography.labelLarge)

                                Text(
                                    text = if (showEarlier) "Hide" else "Show",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Only show list when expanded
                        if (showEarlier) {
                            items(earlierList, key = { it.id }) { n ->
                                NotificationBubble(n, showStartsLabel = false)
                            }
                        }
                    }

                }
            }

        }
    }
}

/* -------------------- UI -------------------- */

@Composable
private fun NotificationBubble(n: InAppNotification, showStartsLabel: Boolean) {
    val bg = if (n.isRead)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    else
        MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { NotificationCenter.markRead(n.id) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(categoryDotColor(n.taskCategory))
        )

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = n.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = timeAgo(n.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (showStartsLabel) n.message else cleanStartsMessage(n.message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
    )
}
@Composable
private fun categoryDotColor(taskCategory: String?): Color {
    return when (taskCategory) {
        "CLASS" -> MaterialTheme.colorScheme.primary
        "EXAM" -> MaterialTheme.colorScheme.error
        "CCA" -> MaterialTheme.colorScheme.tertiary
        "PERSONAL" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
}
