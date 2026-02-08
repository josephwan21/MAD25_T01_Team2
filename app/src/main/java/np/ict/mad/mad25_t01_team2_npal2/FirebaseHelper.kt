package np.ict.mad.mad25_t01_team2_npal2


import android.R.attr.identifier
import android.util.Log
import androidx.room.util.copy
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val studentId: String = "",
    val email: String = ""
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

    private fun usernameFromEmail(email: String): String {
        return email.substringBefore("@")
    }

    private fun generateStudentId(): String {
        val digits = (10000000..99999999).random()
        val letter = ('A'..'Z').random()
        return "S${digits}${letter}"
    }

    suspend fun createUserProfile(user: FirebaseUser): Boolean {
        return try {
            val email = user.email ?: return false
            val username = usernameFromEmail(email)
            val studentId = generateStudentId()

            val profile = UserProfile(
                uid = user.uid,
                username = username,
                studentId = studentId,
                email = email
            )

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .set(profile)
                .await()

            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Create user profile failed", e)
            false
        }
    }



    /*suspend fun signIn(username: String, password: String):Boolean {
        return try{
            val email = toEmail(username)
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user!= null
        } catch (e:Exception){
            Log.e("FirebaseHelper", "Login Failed!", e)
            false
        }
    }*/

    suspend fun signIn(usernameOrEmail: String, password: String): Boolean {
        return try {
            val email = if (usernameOrEmail.contains("@")) {
                // User entered email directly
                usernameOrEmail
            } else {
                val usersRef = FirebaseFirestore.getInstance().collection("users")
                // User entered username, look up in Firestore
                var querySnapshot = usersRef
                    .whereEqualTo("username", usernameOrEmail)
                    .get()
                    .await()

                var userDoc = querySnapshot.documents.firstOrNull()

                if (userDoc == null) {
                    querySnapshot = usersRef.whereEqualTo("studentId", usernameOrEmail).get().await()
                    userDoc = querySnapshot.documents.firstOrNull()
                }

                if (userDoc == null) {
                    Log.d("FirebaseHelper", "No user found for identifier: $usernameOrEmail")
                    return false
                }

                userDoc.getString("email") ?: return false
            }

            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user != null
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Sign-in Failed!", e)
            false
        }
    }


    suspend fun signUp(username: String, password: String): Boolean
    {
        return try{
            val email = toEmail(username)
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return false

            createUserProfile(user)
        } catch (e:Exception){
            Log.e("FirebaseHelper", "Login Failed", e)
            false
        }

    }

    suspend fun reAuthenticate(password: String): Boolean {
        val user = auth.currentUser ?: return false
        val email = user.email ?: return false

        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Re-authentication failed", e)
            false
        }
    }

    suspend fun updateUsername(newUsername: String): Boolean {
        val user = auth.currentUser ?: return false

        return try {
            // Update the username in Firestore only
            val userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)

            // Only update the username field, keep other data intact
            userRef.update("username", newUsername).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Update Username Failed", e)
            false
        }
    }

    suspend fun updatePassword(newPassword: String, currentPassword: String): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            // Re-authenticate first
            val reAuthSuccess = reAuthenticate(currentPassword)
            if (!reAuthSuccess) return false

            user.updatePassword(newPassword).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Update Password Failed", e)
            false
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        return try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .get()
                .await()
                .toObject(UserProfile::class.java) // converts document to UserProfile
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Get user profile failed", e)
            null
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

    suspend fun deleteTask(userId: String, taskId: String): Boolean {
        return try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("TasksfromUser")
                .document(taskId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Delete Task Failed", e)
            false
        }
    }
    suspend fun updateTask(userId: String, task: Task): Boolean {
        return try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("TasksfromUser")
                .document(task.id)
                .set(task)
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Update Task Failed", e)
            false
        }
    }



    suspend fun saveNotificationIfMissing(
        userId: String,
        n: InAppNotification
    ): Boolean {
        return try {
            val docRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notifications")
                .document(n.id) // n.id must be deterministic

            val existing = docRef.get().await()
            if (existing.exists()) return true   // ✅ DO NOT overwrite

            docRef.set(n).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Save notification failed", e)
            false
        }
    }

    suspend fun getNotifications(userId: String, limit: Long = 50): List<InAppNotification> {
        return try {
            val snap = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snap.documents.mapNotNull { it.toObject(InAppNotification::class.java) }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Get notifications failed", e)
            emptyList()
        }
    }
    suspend fun deleteAllNotifications(userId: String): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .await()

            val batch = db.batch()
            snap.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Delete all notifications failed", e)
            false
        }
    }

    suspend fun markAllNotificationsRead(userId: String): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .await()

            val batch = db.batch()
            snap.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)

            }
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Mark all notifications read failed", e)
            false
        }
    }


}

