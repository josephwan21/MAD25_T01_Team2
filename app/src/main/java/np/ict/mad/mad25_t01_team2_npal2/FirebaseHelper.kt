package np.ict.mad.mad25_t01_team2_npal2


import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

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
            Log.e("FirebaseHelper", "Login Failed!", e)
            false
        }

    }
}
