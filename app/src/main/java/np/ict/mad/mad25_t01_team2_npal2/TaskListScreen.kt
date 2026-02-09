package np.ict.mad.mad25_t01_team2_npal2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.api.Context
import kotlinx.coroutines.launch
import np.ict.mad.mad25_t01_team2_npal2.ui.theme.MAD25_T01_Team2_NPAL2Theme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



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

data class DayItem(
    val dayName: String,
    val dateNumber: Int,
    val fullDate: String
)

@Composable
fun TaskListScreenContent(
    firebaseHelper: FirebaseHelper,
    userId: String,
    onCreateTask: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {

    /*val monthYear = "December 2025"

    val days = listOf(
        "Mon" to 17,
        "Tue" to 18,
        "Wed" to 19,
        "Thu" to 20,
        "Fri" to 21,
        "Sat" to 22,
        "Sun" to 23
    )*/

    val times = (10..20).toList() // 10 AM to 8 PM

    // Dummy tasks mapped to specific times
    /*val tasks = mapOf(
        10 to "Complete Project Proposal",
        13 to "Review DDV Assignment",
        16 to "Consultation with Lecturer"
    )*/
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    val scope = rememberCoroutineScope()

    var editingTask by remember { mutableStateOf<Task?>(null) }
    val context = LocalContext.current

    //val tasksByHour = groupTasksByHour(tasks)
    LaunchedEffect(userId) {
        tasks = firebaseHelper.getTasks(userId)
        TaskReminder.run(context, firebaseHelper, userId, tasks)
    }



    if (editingTask != null) {
        EditTaskScreen(
            task = editingTask!!,
            userId = userId,
            firebaseHelper = firebaseHelper,
            onBack = {
                editingTask = null
            },
            onTaskUpdated = {
                scope.launch {
                    tasks = firebaseHelper.getTasks(userId)
                    editingTask = null
                }
            }
        )
    } else {
        TaskListUI(
            tasks = tasks,
            userId = userId,
            onCreateTask = onCreateTask,
            onOpenNotifications = onOpenNotifications,
            onDeleteTask = { task ->
                scope.launch {
                    firebaseHelper.deleteTask(userId, task.id)
                    tasks = firebaseHelper.getTasks(userId)
                }
            },
            onEditTask = { task ->
                editingTask = task
            }
        )
    }


    /*

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
        }*/
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListUI(
    tasks: List<Task>,
    onCreateTask: () -> Unit,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    userId: String,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {

    val times = (0..23).toList()
    //val tasksByHour = groupTasksByHour(tasks)

    val calendar = Calendar.getInstance()
    val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

    val days = remember { getNext7Days() }

    var selectedDay by remember { mutableStateOf(days.first()) }


    // Filter tasks for selected day
    val tasksForDay = tasks.filter { task ->
        task.date == selectedDay.fullDate
    }



    val tasksByHour = groupTasksByHour(tasksForDay)

    Log.d("TaskListUI", "Selected day: ${selectedDay.fullDate}")
    tasksForDay.forEach { Log.d("TaskListUI", "Task: ${it.title}, date=${it.date}") }

    Scaffold(
        topBar = {
            val allNotifs by NotificationCenter.notifications.collectAsState()
            val unread = allNotifs.count { it.userId == userId && !it.isRead }

            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    IconButton(onClick = onOpenNotifications) {
                        BadgedBox(
                            badge = {
                                if (unread > 0) {
                                    Badge { Text(unread.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) {
                Icon(Icons.Default.Add, contentDescription = "Create Task")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = monthYear,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(16.dp))

            // ---------------------------
            // Horizontal 7-day selector
            // ---------------------------
            LazyRow {
                items(days) { day ->
                    val isSelected = day.fullDate == selectedDay.fullDate

                    Card(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .width(70.dp)
                            .height(70.dp)
                            .clickable { selectedDay = day},
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(day.dayName, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) Color.White else Color.Black)
                            Text(day.dateNumber.toString(), style = MaterialTheme.typography.titleMedium, color = if (isSelected) Color.White else Color.Black)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)

            ) {
                items(times) { hour ->

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {

                        // left time label
                        Text(
                            formatHour(hour),
                            modifier = Modifier.width(60.dp)
                        )

                        // right task OR empty
                        val tasksAtThisHour = tasksByHour[hour]

                        if (tasksAtThisHour != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
                            ) {
                                tasksAtThisHour.forEach { task ->
                                    var menuExpanded by remember { mutableStateOf(false) }
                                    val cat = categoryFromString(task.category)

                                    val borderColor = when (cat) {
                                        TaskCategory.CLASS -> MaterialTheme.colorScheme.primary
                                        TaskCategory.EXAM -> MaterialTheme.colorScheme.error
                                        TaskCategory.CCA -> MaterialTheme.colorScheme.tertiary
                                        TaskCategory.PERSONAL -> MaterialTheme.colorScheme.secondary
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Text(task.title, style = MaterialTheme.typography.titleMedium)
                                                Text(task.description, style = MaterialTheme.typography.bodyMedium)
                                            }

                                            // RIGHT: time + ellipsis
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "${formatTo12Hour(task.startTime)} – ${formatTo12Hour(task.endTime)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    textAlign = TextAlign.End
                                                )

                                                Box {
                                                    IconButton(onClick = { menuExpanded = true }) {
                                                        Icon(
                                                            imageVector = Icons.Default.MoreVert,
                                                            contentDescription = "Task options"
                                                        )
                                                    }

                                                    DropdownMenu(
                                                        expanded = menuExpanded,
                                                        onDismissRequest = { menuExpanded = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Edit") },
                                                            onClick = {
                                                                menuExpanded = false
                                                                onEditTask(task)
                                                            }
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Delete") },
                                                            onClick = {
                                                                menuExpanded = false
                                                                onDeleteTask(task)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun formatTo12Hour(time24: String): String {
    return try {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(time24)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        time24 // fallback if parsing fails
    }
}
@Composable
fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
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
    val textFieldValueSaver = Saver<TextFieldValue, String>(
        save = { it.text },                  // how to save: just the text
        restore = { TextFieldValue(it) }     // how to restore: make a new TextFieldValue from text
    )
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable(stateSaver = textFieldValueSaver) { mutableStateOf(TextFieldValue("")) }
    var startTime by rememberSaveable(stateSaver = textFieldValueSaver) { mutableStateOf(TextFieldValue("")) }
    var endTime by rememberSaveable(stateSaver = textFieldValueSaver) { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by rememberSaveable { mutableStateOf(TaskCategory.PERSONAL) }
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
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            CategoryPicker(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
            Spacer(Modifier.height(12.dp))
            DatePickerField(date = date, onDateSelected = { date = it })
            Spacer(Modifier.height(12.dp))
            StartTimePickerField(time = startTime, onTimeSelected = { startTime = it })
            Spacer(Modifier.height(12.dp))
            EndTimePickerField(time = endTime, onTimeSelected = { endTime = it })


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
                    scope.launch {

                        if (title.isBlank() || description.isBlank() || date.text.isBlank() ||
                            startTime.text.isBlank() || endTime.text.isBlank()
                        ) {
                            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        try {
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val start = timeFormat.parse(startTime.text)
                            val end = timeFormat.parse(endTime.text)

                            if (start!!.after(end)) {
                                Toast.makeText(
                                    context,
                                    "End time must be later than start time",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            val formattedDate = try {
                                val inputFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault()) // whatever comes from DatePicker
                                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val parsedDate = inputFormat.parse(date.text)
                                outputFormat.format(parsedDate!!)
                            } catch (e: Exception) {
                                date.text // fallback if parsing fails
                            }
                            val task = Task(
                                id = "",
                                userId = userId,
                                title = title,
                                description = description,
                                date = formattedDate,
                                startTime = startTime.text,
                                endTime = endTime.text,
                                category = selectedCategory.name

                            )

                            val success = firebaseHelper.saveTask(userId, task)
                            if (success) {
                                Toast.makeText(context, "Task saved!", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                            Toast.makeText(context, "Failed to save task", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Invalid time format", Toast.LENGTH_SHORT).show()
                        }
                    }
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
fun EditTaskScreen(
    task: Task,
    onBack: () -> Unit,
    onTaskUpdated: () -> Unit,
    firebaseHelper: FirebaseHelper,
    userId: String
) {
    val textFieldValueSaver = Saver<TextFieldValue, String>(
        save = { it.text },
        restore = { TextFieldValue(it) }
    )
    var title by rememberSaveable { mutableStateOf(task.title) }
    var description by rememberSaveable { mutableStateOf(task.description) }
    var date by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(TextFieldValue(task.date))
    }
    var startTime by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(TextFieldValue(task.startTime))
    }
    var endTime by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(TextFieldValue(task.endTime))
    }
    var selectedCategory by rememberSaveable {
        mutableStateOf(TaskCategory.valueOf(task.category))
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            CategoryPicker(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            Spacer(Modifier.height(12.dp))

            DatePickerField(
                date = date,
                onDateSelected = { date = it }
            )

            Spacer(Modifier.height(12.dp))

            StartTimePickerField(
                time = startTime,
                onTimeSelected = { startTime = it }
            )

            Spacer(Modifier.height(12.dp))

            EndTimePickerField(
                time = endTime,
                onTimeSelected = { endTime = it }
            )

            Spacer(Modifier.height(24.dp))

            // ---- Save Changes ----
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {

                        if (title.isBlank() || description.isBlank() ||
                            date.text.isBlank() || startTime.text.isBlank() || endTime.text.isBlank()
                        ) {
                            Toast.makeText(
                                context,
                                "All fields are required",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        try {
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val start = timeFormat.parse(startTime.text)
                            val end = timeFormat.parse(endTime.text)

                            if (start!!.after(end)) {
                                Toast.makeText(
                                    context,
                                    "End time must be later than start time",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            val updatedTask = task.copy(
                                title = title,
                                description = description,
                                date = date.text,
                                startTime = startTime.text,
                                endTime = endTime.text,
                                category = selectedCategory.name
                            )

                            val success = firebaseHelper.updateTask(userId, updatedTask)

                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Task updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onTaskUpdated()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to update task",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Invalid time format",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            ) {
                Text("Save Changes")
            }

            Spacer(Modifier.height(12.dp))

            // ---- Delete Task ----
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                onClick = {
                    scope.launch {
                        val success = firebaseHelper.deleteTask(userId, task.id)
                        if (success) {
                            Toast.makeText(
                                context,
                                "Task deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            onTaskUpdated()
                            onBack()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to delete task",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            ) {
                Text("Delete Task")
            }
        }
    }
}

@Composable
fun CategoryPicker(
    selected: TaskCategory,
    onSelect: (TaskCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TaskCategory.values().forEach { cat ->
            val isSelected = cat == selected

            val bgColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface

            val textColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(cat) },
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                border = if (!isSelected)
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                else null
            ) {
                Text(
                    text = cat.label,
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}


fun getNext7Days(): List<DayItem> {
    val calendar = Calendar.getInstance()
    val days = mutableListOf<DayItem>()

    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    repeat(7) {
        val dayName = dayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val dateNumber = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        val fullDate = String.format("%04d-%02d-%02d", year, month, dateNumber)


        days.add(DayItem(dayName, dateNumber, fullDate))

        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    return days
}

fun groupTasksByHour(tasks: List<Task>): Map<Int, List<Task>> {
    return tasks.groupBy { task ->
        task.startTime.take(2).toInt() // "13:00" → 13
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
    date: TextFieldValue,
    onDateSelected: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    OutlinedTextField(
        value = date,
        onValueChange = { input ->
            val digits = input.text.filter { it.isDigit() }.take(8)

            val formatted = when (digits.length) {
                in 0..4 -> digits
                in 5..6 -> "${digits.substring(0,4)}-${digits.substring(4)}"
                else -> "${digits.substring(0,4)}-${digits.substring(4,6)}-${digits.substring(6)}"
            }

            val cursorPos = formatted.length

            onDateSelected(
                TextFieldValue(
                    text = formatted,
                    selection = TextRange(cursorPos)
                )
            )
        },
        label = { Text("Date (yyyy-MM-dd)") },
        trailingIcon = {
            Icon(Icons.Default.DateRange,
                contentDescription = "Select date",
                modifier = Modifier.clickable { // Icon is clickable too
                    val datePicker = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val formatted = String.format(
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                dayOfMonth
                                )
                                onDateSelected(
                                    TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length)
                                    )
                                )
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
                        val formatted = String.format(
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                        )
                        onDateSelected(
                            TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
    )
}

@Composable
fun StartTimePickerField(
    time: TextFieldValue,
    onTimeSelected: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    OutlinedTextField(
        value = time,
        onValueChange = { input ->
            val digits = input.text.filter { it.isDigit() }.take(4)

            val formatted = when (digits.length) {
                0, 1, 2 -> digits
                3 -> "${digits.substring(0,2)}:${digits.substring(2)}"
                else -> "${digits.substring(0,2)}:${digits.substring(2,4)}"
            }

            val cursorPos = formatted.length

            onTimeSelected(
                TextFieldValue(
                    text = formatted,
                    selection = TextRange(cursorPos)
                )
            )
        },
        label = { Text("Start Time") },
        trailingIcon = {
            Icon( Icons.Default.Create,
                contentDescription = "Select time",
                modifier = Modifier.clickable {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            val formatted = String.format("%02d:%02d", hourOfDay, minute)
                            onTimeSelected(
                                TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                            )
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
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
                        val formatted = String.format("%02d:%02d", hourOfDay, minute)
                        onTimeSelected(
                            TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        )
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }

    )
}
@Composable
fun EndTimePickerField(
    time: TextFieldValue,
    onTimeSelected: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    OutlinedTextField(
        value = time,
        onValueChange = { input ->
            val digits = input.text.filter { it.isDigit() }.take(4)

            val formatted = when (digits.length) {
                0, 1, 2 -> digits
                3 -> "${digits.substring(0,2)}:${digits.substring(2)}"
                else -> "${digits.substring(0,2)}:${digits.substring(2,4)}"
            }

            val cursorPos = formatted.length

            onTimeSelected(
                TextFieldValue(
                    text = formatted,
                    selection = TextRange(cursorPos)
                )
            )
        },
        label = { Text("End Time") },
        trailingIcon = {
            Icon( Icons.Default.Create,
                contentDescription = "Select time",
                modifier = Modifier.clickable {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            val formatted = String.format("%02d:%02d", hourOfDay, minute)
                            onTimeSelected(
                                TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                            )
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
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
                        val formatted = String.format("%02d:%02d", hourOfDay, minute)
                        onTimeSelected(
                            TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        )
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }

    )
}
fun categoryFromString(value: String): TaskCategory {
    return runCatching { TaskCategory.valueOf(value) }
        .getOrElse { TaskCategory.PERSONAL }
}



