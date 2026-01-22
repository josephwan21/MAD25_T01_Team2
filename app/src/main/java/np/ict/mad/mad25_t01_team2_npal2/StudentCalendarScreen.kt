package np.ict.mad.mad25_t01_team2_npal2

import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

data class CalendarTask( //
    val title: String,
    val time: String,
    val category: TaskCategory
)


@Composable
fun StudentCalendarScreen(
    firebaseHelper: FirebaseHelper,
    userId: String
) {
    // Month names and days per month
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

    // Today from system (for highlight)
    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)      // 0–11
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    // Calendar state
    var year by remember { mutableStateOf(todayYear) }
    var monthIndex by remember { mutableStateOf(todayMonth) }
    var selectedDay by remember { mutableStateOf(todayDay) }

    val daysInMonth = daysInMonthList[monthIndex]
    if (selectedDay > daysInMonth) selectedDay = daysInMonth

    val monthLabel = "${monthNames[monthIndex]}, $year"

    // Load tasks from Firebase
    var allTasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    LaunchedEffect(userId) {
        allTasks = firebaseHelper.getTasks(userId)
    }

    // Convert Firebase tasks into calendar task map
    val tasksMap: Map<Triple<Int, Int, Int>, List<CalendarTask>> =
        allTasks
            .mapNotNull { task ->
                val ymd = parseYMD(task.date) ?: return@mapNotNull null
                ymd to task.toCalendarTask()
            }
            .groupBy({ it.first }, { it.second })

    val tasksForSelectedDay =
        tasksMap[Triple(year, monthIndex, selectedDay)] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        // Push header a bit down
        Spacer(modifier = Modifier.height(32.dp))

        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        // previous month
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
                        // next month
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

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday header
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

        // Calendar grid
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
                                            if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurface,
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

        Spacer(modifier = Modifier.height(24.dp))

        // Tasks Section
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

                    val borderColor = when (task.category) {
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
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {

                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Text(
                                text = task.time,
                                fontSize = 12.sp
                            )

                            Text(
                                text = task.category.label,
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
}

private fun parseYMD(date: String): Triple<Int, Int, Int>? {
    val parts = date.split("-")
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    return Triple(y, m - 1, d)
}

private fun Task.toCalendarTask(): CalendarTask {
    val cat = runCatching { TaskCategory.valueOf(category) }
        .getOrElse { TaskCategory.PERSONAL }

    return CalendarTask(
        title = title,
        time = "${formatTo12Hour(startTime)} – ${formatTo12Hour(endTime)}",
        category = cat
    )
}


private fun hasEventsOnDay(
    year: Int,
    month: Int,
    day: Int,
    tasks: Map<Triple<Int, Int, Int>, List<CalendarTask>>
): Boolean {
    return tasks[Triple(year, month, day)]?.isNotEmpty() == true
}
