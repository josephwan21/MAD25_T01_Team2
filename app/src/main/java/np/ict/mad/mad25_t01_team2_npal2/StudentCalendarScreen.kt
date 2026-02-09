package np.ict.mad.mad25_t01_team2_npal2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

data class CalendarTask(
    val title: String,
    val time: String,
    val category: TaskCategory
)

@Composable
fun StudentCalendarScreen(
    firebaseHelper: FirebaseHelper,
    userId: String
) {
    val monthNames = listOf(
        "January", "February", "March", "April",
        "May", "June", "July", "August",
        "September", "October", "November", "December"
    )

    val daysInMonthList = listOf(
        31, 28, 31, 30,
        31, 30, 31, 31,
        30, 31, 30, 31
    )

    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    var year by remember { mutableStateOf(todayYear) }
    var monthIndex by remember { mutableStateOf(todayMonth) }
    var selectedDay by remember { mutableStateOf(todayDay) }

    val daysInMonth = daysInMonthList[monthIndex]
    if (selectedDay > daysInMonth) selectedDay = daysInMonth

    val monthLabel = "${monthNames[monthIndex]}, $year"

    var allTasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    LaunchedEffect(userId) {
        allTasks = firebaseHelper.getTasks(userId)
    }

    // Map date -> list of original Firebase Tasks (keeps description)
    val tasksMap: Map<Triple<Int, Int, Int>, List<Task>> =
        allTasks
            .mapNotNull { task ->
                val ymd = parseYMD(task.date) ?: return@mapNotNull null
                ymd to task
            }
            .groupBy({ it.first }, { it.second })

    val tasksForSelectedDay: List<Task> =
        tasksMap[Triple(year, monthIndex, selectedDay)] ?: emptyList()

    // Selected task for dialog
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()          //  pushes below camera/status bar
            .padding(horizontal = 16.dp)
            .padding(top = 0.dp)          // small extra breathing space
    ) {


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        if (monthIndex == 0) {
                            monthIndex = 11
                            year -= 1
                        } else {
                            monthIndex -= 1
                        }
                        selectedDay = 1
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month"
                )
            }

            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        if (monthIndex == 11) {
                            monthIndex = 0
                            year += 1
                        } else {
                            monthIndex += 1
                        }
                        selectedDay = 1
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column {
            var day = 1
            for (week in 0 until 6) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (d in 0 until 7) {
                        if (day <= daysInMonth) {

                            val dayNumber = day
                            val isSelected = dayNumber == selectedDay
                            val isToday =
                                year == todayYear &&
                                        monthIndex == todayMonth &&
                                        dayNumber == todayDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .clickable { selectedDay = dayNumber },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color =
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface,
                                        fontWeight =
                                            if (isSelected) FontWeight.Bold
                                            else FontWeight.Normal
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    if (hasEventsOnDay(year, monthIndex, dayNumber, tasksMap)) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondary)
                                        )
                                    }
                                }
                            }
                            day++
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))


        Text(
            text = "Tasks for $selectedDay $monthLabel",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (tasksForSelectedDay.isEmpty()) {
            Text("No tasks for today.", fontSize = 14.sp)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tasksForSelectedDay) { task ->

                    val taskCategory = runCatching { TaskCategory.valueOf(task.category) }
                        .getOrElse { TaskCategory.PERSONAL }

                    val borderColor = when (taskCategory) {
                        TaskCategory.CLASS -> MaterialTheme.colorScheme.primary
                        TaskCategory.EXAM -> MaterialTheme.colorScheme.error
                        TaskCategory.CCA -> MaterialTheme.colorScheme.tertiary
                        TaskCategory.PERSONAL -> MaterialTheme.colorScheme.secondary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTask = task },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {

                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "${formatTo12Hour(task.startTime)} – ${formatTo12Hour(task.endTime)}",
                                fontSize = 12.sp
                            )

                            Text(
                                text = taskCategory.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = borderColor
                            )
                        }
                    }
                }
            }
        }
    }

    //  show full details of task
    selectedTask?.let { task ->
        val taskCategory = runCatching { TaskCategory.valueOf(task.category) }
            .getOrElse { TaskCategory.PERSONAL }

        AlertDialog(
            onDismissRequest = { selectedTask = null },
            confirmButton = {
                TextButton(onClick = { selectedTask = null }) {
                    Text("Close")
                }
            },
            title = { Text(task.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Time: ${formatTo12Hour(task.startTime)} – ${formatTo12Hour(task.endTime)}")
                    Text("Category: ${taskCategory.label}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (task.description.isNotBlank()) task.description else "No description provided.",
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}

private fun parseYMD(date: String): Triple<Int, Int, Int>? {
    val parts = date.split("-")
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    return Triple(y, m - 1, d)
}

private fun hasEventsOnDay(
    year: Int,
    month: Int,
    day: Int,
    tasks: Map<Triple<Int, Int, Int>, List<Task>>
): Boolean {
    return tasks[Triple(year, month, day)]?.isNotEmpty() == true
}
