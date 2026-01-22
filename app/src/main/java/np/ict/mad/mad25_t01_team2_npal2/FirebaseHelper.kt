package np.ict.mad.mad25_t01_team2_npal2


import android.util.Log
import androidx.room.util.copy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val category: String = TaskCategory.PERSONAL.name
)

class FirebaseHelper{

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun toEmail(username:String): String {
        return if(username.contains("@")){
            username
        }else{
            "$username@gmail.com" //placeholder email
        }
    }

    suspend fun signIn(username: String, password: String):Boolean {
        return try{
            val email = toEmail(username)
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user!= null
        } catch (e:Exception){
            Log.e("FirebaseHelper", "Login Failed!", e)
            false
        }
    }

    suspend fun signUp(username: String, password: String): Boolean
    {
        return try{
            val email = toEmail(username)
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user!= null
        } catch (e:Exception){
            Log.e("FirebaseHelper", "Login Failed", e)
            false
        }

    }

    suspend fun saveTask(userId: String, task: Task): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val newDoc = db.collection("users")
                .document(userId)
                .collection("TasksfromUser")
                .document()

            val taskWithId = task.copy(id = newDoc.id)
            newDoc.set(taskWithId).await()

            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Save Task Failed", e)
            false
        }
    }

    suspend fun getTasks(userId: String): List<Task> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("users")
                .document(userId)
                .collection("TasksfromUser")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(Task::class.java) }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Fetching tasks failed", e)
            emptyList()
        }
    }


}
