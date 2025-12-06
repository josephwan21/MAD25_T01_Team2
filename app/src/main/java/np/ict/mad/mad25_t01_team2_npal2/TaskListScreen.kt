package np.ict.mad.mad25_t01_team2_npal2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.ict.mad.mad25_t01_team2_npal2.ui.theme.MAD25_T01_Team2_NPAL2Theme

/*class TaskListScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MAD25_T01_Team2_NPAL2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TaskListScreenContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}*/

/*@Composable
fun TaskListScreenContent(modifier: Modifier = Modifier) {
    val tasks = listOf("Task 1", "Task 2", "Task 3", "Task 4", "Task 5")
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(tasks) { task ->
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(task, modifier = Modifier.padding(16.dp))
            }
        }
    }
}*/

@Composable
fun TaskListScreenContent(modifier: Modifier = Modifier) {

    val monthYear = "November 2025"

    val days = listOf(
        "Mon" to 17,
        "Tue" to 18,
        "Wed" to 19,
        "Thu" to 20,
        "Fri" to 21,
        "Sat" to 22,
        "Sun" to 23
    )

    val times = (10..20).toList() // 10 AM to 8 PM

    // Dummy tasks mapped to specific times
    val tasks = mapOf(
        10 to "Complete Project Proposal",
        13 to "Review DDV Assignment",
        16 to "Consultation with Lecturer"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month + Year
        Text(
            text = monthYear,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7-day selector row
        LazyRow {
            items(days) { (day, date) ->
                Card(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(70.dp)
                        .height(70.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(day, style = MaterialTheme.typography.bodyMedium)
                        Text(date.toString(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Timeline
        LazyColumn {
            items(times) { hour ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Time Label
                    Text(
                        text = formatHour(hour),
                        modifier = Modifier.width(60.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Task Box OR empty space
                    if (tasks.containsKey(hour)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                tasks[hour]!!,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun formatHour(hour: Int): String {
    return when {
        hour == 12 -> "12 PM"
        hour > 12 -> "${hour - 12} PM"
        else -> "$hour AM"
    }
}

