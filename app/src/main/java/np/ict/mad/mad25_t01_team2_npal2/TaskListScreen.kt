package np.ict.mad.mad25_t01_team2_npal2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.ict.mad.mad25_t01_team2_npal2.ui.theme.MAD25_T01_Team2_NPAL2Theme

class TaskListScreen : ComponentActivity() {
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
}

@Composable
fun TaskListScreenContent(modifier: Modifier = Modifier) {
    val tasks = listOf("Task 1", "Task 2", "Task 3", "Task 4", "Task 5")
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(tasks) { task ->
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(task, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
