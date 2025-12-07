package np.ict.mad.mad25_t01_team2_npal2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Task
import com.google.api.Context
import kotlinx.coroutines.launch
import np.ict.mad.mad25_t01_team2_npal2.ui.theme.MAD25_T01_Team2_NPAL2Theme
import java.util.Calendar

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
fun TaskListScreenContent(
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {

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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) {
                Icon(Icons.Default.Add, contentDescription = "Create Task")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
}




@Composable
fun formatHour(hour: Int): String {
    return when {
        hour == 12 -> "12 PM"
        hour > 12 -> "${hour - 12} PM"
        else -> "$hour AM"
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    firebaseHelper: FirebaseHelper,
    userId: String
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("") }
    var endTime by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth()
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            DatePickerField(date = date, onDateSelected = { date = it })
            Spacer(Modifier.height(12.dp))
            TimePickerField(time = startTime, onTimeSelected = { startTime = it })
            Spacer(Modifier.height(12.dp))
            TimePickerField(time = endTime, onTimeSelected = { endTime = it })


            /*OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (e.g. 2025-01-08)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = { Text("Start Time (e.g. 10:00)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it },
                label = { Text("End Time (e.g. 12:00)") },
                modifier = Modifier.fillMaxWidth()
            )*/

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    // TODO save to Firebase or local list
                    scope.launch {
                        val task = Task(
                            id = "",
                            userId = userId,
                            title = title,
                            description = description,
                            date = date,
                            startTime = startTime,
                            endTime = endTime
                        )

                        val success = firebaseHelper.saveTask(userId, task)
                        if (success) {
                            Toast.makeText(context, "Task saved!", Toast.LENGTH_SHORT).show()
                            onBack()
                        } else {
                            Toast.makeText(context, "Failed to save task", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Task")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListTopBar() {
    CenterAlignedTopAppBar(
        title = { Text("Tasks") }
    )
}

@Composable
fun DatePickerField(
    date: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Wrapper Box to handle clicks
    Box(modifier = modifier.clickable {
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected("$year-${month + 1}-$dayOfMonth")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    })

    OutlinedTextField(
        value = date,
        onValueChange = {},
        label = { Text("Date") },
        readOnly = true,
        trailingIcon = {
            Icon(Icons.Default.DateRange,
                contentDescription = "Select date",
                modifier = Modifier.clickable { // Icon is clickable too
                    val datePicker = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            onDateSelected("$year-${month + 1}-$dayOfMonth")
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        onDateSelected("$year-${month + 1}-$dayOfMonth")
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
    )
}

@Composable
fun TimePickerField(
    time: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    OutlinedTextField(
        value = time,
        onValueChange = {},
        label = { Text("Time") },
        readOnly = true,
        trailingIcon = {
            Icon( Icons.Default.Create,
                contentDescription = "Select time",
                modifier = Modifier.clickable {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onTimeSelected(String.format("%02d:%02d", hourOfDay, minute))
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        onTimeSelected(String.format("%02d:%02d", hourOfDay, minute))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }

    )
}

