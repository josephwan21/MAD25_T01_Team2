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
    val endTime: String = ""
)

data class LocationFeedback(
    val id: String = "",
    val userId: String = "",
    val locationName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class FirebaseHelper{

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun toEmail(username:String): String {
        return if(username.contains("@")){
            username
        }else{
            "$username@gmail.com"
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

    suspend fun saveLocationFeedback(
        userId: String,
        locationName: String,
        rating: Int,
        comment: String
    ): Boolean {
        return try {
            val newDoc = db.collection("locations")
                .document(locationName)
                .collection("reviews")
                .document()

            val feedback = LocationFeedback(
                id = newDoc.id,
                userId = userId,
                locationName = locationName,
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )

            newDoc.set(feedback).await()

            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Save Feedback Failed", e)
            false
        }
    }

    suspend fun getLocationFeedback(locationName: String): List<LocationFeedback> {
        return try {
            val snapshot = db.collection("locations")
                .document(locationName)
                .collection("reviews")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(LocationFeedback::class.java) }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Fetching feedback failed", e)
            emptyList()
        }
    }

    suspend fun getUserFeedbackForLocation(userId: String, locationName: String): LocationFeedback? {
        return try {
            val snapshot = db.collection("locations")
                .document(locationName)
                .collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(LocationFeedback::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Fetching user feedback failed", e)
            null
        }
    }

    suspend fun getAverageRating(locationName: String): Float {
        return try {
            val feedbacks = getLocationFeedback(locationName)
            if (feedbacks.isEmpty()) {
                0f
            } else {
                feedbacks.map { it.rating }.average().toFloat()
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Calculating average rating failed", e)
            0f
        }
    }

    suspend fun updateLocationFeedback(
        feedbackId: String,
        locationName: String,
        rating: Int,
        comment: String
    ): Boolean {
        return try {
            db.collection("locations")
                .document(locationName)
                .collection("reviews")
                .document(feedbackId)
                .update(
                    mapOf(
                        "rating" to rating,
                        "comment" to comment,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
                .await()

            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Update Feedback Failed", e)
            false
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}