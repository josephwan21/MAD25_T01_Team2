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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/* -------------------- DATA -------------------- */

enum class NotificationKind { GENERIC, OVERDUE }

data class InAppNotification(
    val id: String = "",
    val key: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val kind: NotificationKind = NotificationKind.GENERIC,
    val taskCategory: String? = null,
    val dismissed: Boolean = false
)

/* -------------------- CENTER -------------------- */

object NotificationCenter {

    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications

    private val pushedKeys = mutableSetOf<String>()

    fun setAll(list: List<InAppNotification>) {
        val localMap = _notifications.value.associateBy { it.id }

        val merged = list.map { remote ->
            val local = localMap[remote.id]
            if (local == null) remote
            else remote.copy(
                isRead = remote.isRead || local.isRead,
                dismissed = remote.dismissed || local.dismissed
            )
        }

        _notifications.value = merged
        pushedKeys.clear()
        pushedKeys.addAll(merged.mapNotNull { it.key.ifBlank { null } })
    }

    fun pushOnce(
        context: Context,
        firebaseHelper: FirebaseHelper,
        key: String,
        userId: String,
        title: String,
        message: String,
        timestamp: Long = System.currentTimeMillis(),
        kind: NotificationKind = NotificationKind.GENERIC,
        taskCategory: String? = null
    ) {
        if (!isNotificationsEnabled(context)) return
        if (key.isBlank()) return

        val alreadyDismissed = _notifications.value.any {
            (it.id == key || it.key == key) && it.dismissed
        }
        if (alreadyDismissed) return


        // prevent duplicates
        if (pushedKeys.contains(key)) return
        if (_notifications.value.any { it.id == key }) return

        pushedKeys.add(key)

        val n = InAppNotification(
            id = key,
            key = key,
            userId = userId,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = false,
            kind = kind,
            taskCategory = taskCategory,
            dismissed = false
        )

        _notifications.value = listOf(n) + _notifications.value

        CoroutineScope(Dispatchers.IO).launch {
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

    fun dismiss(id: String) {
        val item = _notifications.value.firstOrNull { it.id == id }
        if (item != null && item.key.isNotBlank()) {
            pushedKeys.add(item.key) // extra
        }

        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(dismissed = true) else it
        }
    }

    fun dismissAll(userId: String) {
        _notifications.value = _notifications.value.map {
            if (it.userId == userId) it.copy(dismissed = true) else it
        }
    }


    fun unreadCount(userId: String): Int {
        return _notifications.value.count {
            it.userId == userId && !it.isRead && !it.dismissed
        }
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
        .replace("Starts in 1 hour", "")
        .replace("Starts at", "")
        .replace("•", "")
        .replace("·", "")
        .replace("-", "")
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

    if (userId.isBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val context = LocalContext.current
    val notificationsEnabled = isNotificationsEnabled(context)

    val todayStart = startOfTodayMillis()
    val yesterdayStart = todayStart - 24L * 60L * 60L * 1000L

    val userNotifications = all
        .filter { it.userId == userId && !it.dismissed }
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
                    TextButton(
                        enabled = userNotifications.isNotEmpty(),
                        onClick = {
                            NotificationCenter.markAllRead(userId)
                            scope.launch { firebaseHelper.markAllNotificationsRead(userId) }
                        }
                    ) { Text("Mark all read") }
                    TextButton(
                        enabled = userNotifications.isNotEmpty(),
                        onClick = {
                            NotificationCenter.dismissAll(userId)
                            scope.launch {
                                firebaseHelper.dismissAllNotifications(userId)
                            }
                        }
                    ) { Text("Clear All") }
                }


            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            if (!notificationsEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Notifications are turned off",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Turn them on in Settings to receive task reminders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (userNotifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (!notificationsEnabled)
                                "Enable notifications to start receiving reminders."
                            else
                                "You’ll see reminders here when tasks are coming up.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {

                    if (todayList.isNotEmpty()) {
                        item { SectionHeader("Today") }
                        items(todayList, key = { it.id }) { n ->
                            DismissibleNotificationItem(
                                n = n,
                                showStartsLabel = true,
                                firebaseHelper = firebaseHelper,
                                userId = userId
                            )
                        }
                    }

                    if (yesterdayList.isNotEmpty()) {
                        item { SectionHeader("Yesterday") }
                        items(yesterdayList, key = { it.id }) { n ->
                            DismissibleNotificationItem(
                                n = n,
                                showStartsLabel = false,
                                firebaseHelper = firebaseHelper,
                                userId = userId
                            )
                        }
                    }

                    if (earlierList.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEarlier = !showEarlier }
                                    .padding(top = 8.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionHeader("Earlier")
                                Text(
                                    text = if (showEarlier) "Hide" else "Show",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (showEarlier) {
                            items(earlierList, key = { it.id }) { n ->
                                DismissibleNotificationItem(
                                    n = n,
                                    showStartsLabel = false,
                                    firebaseHelper = firebaseHelper,
                                    userId = userId
                                )
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
private fun NotificationBubble(
    n: InAppNotification,
    showStartsLabel: Boolean,
    firebaseHelper: FirebaseHelper,
    userId: String
) {
    val scope = rememberCoroutineScope()

    val bg = if (n.isRead)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    else
        MaterialTheme.colorScheme.surfaceVariant

    val cleaned = cleanStartsMessage(n.message)
    val dotColor = categoryDotColor(n.taskCategory)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable {
                if (!n.isRead) {
                    // update UI instantl
                    NotificationCenter.markRead(n.id)

                    // persist to Firestore
                    scope.launch {
                        firebaseHelper.markNotificationRead(userId, n.id)
                    }
                }
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(dotColor)
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = n.title,
                    fontWeight = if (n.isRead) FontWeight.Medium else FontWeight.SemiBold,
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
                text = if (showStartsLabel)
                    "Starts at $cleaned"
                else
                    cleaned,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleNotificationItem(
    n: InAppNotification,
    showStartsLabel: Boolean,
    firebaseHelper: FirebaseHelper,
    userId: String
) {
    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart ||
                value == SwipeToDismissBoxValue.StartToEnd
            ) {
                NotificationCenter.dismiss(n.id)
                scope.launch { firebaseHelper.dismissNotification(userId, n.id) }
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
            )
        }
    ) {
        NotificationBubble(
            n = n,
            showStartsLabel = showStartsLabel,
            firebaseHelper = firebaseHelper,
            userId = userId
        )
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
