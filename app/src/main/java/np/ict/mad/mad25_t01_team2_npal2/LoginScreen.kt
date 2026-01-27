package np.ict.mad.mad25_t01_team2_npal2

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import np.ict.mad.mad25_t01_team2_npal2.ui.theme.MAD25_T01_Team2_NPAL2Theme
import java.util.concurrent.Executor

/*class LoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MAD25_T01_Team2_NPAL2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        onLoginSuccess = {
                            val intent = Intent(this@LoginScreen, HomeScreen::class.java)
                            startActivity(intent)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }
    }
}*/

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
){
    var username by rememberSaveable { mutableStateOf("")}
    var password by rememberSaveable {mutableStateOf("")}
    var loginVerified by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) } // Toggle state

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? ComponentActivity



    // Check if biometrics are available
    val biometricAvailable = remember { activity?.let { isBiometricAvailable(it) } ?: false }

    Box(
        modifier = modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(id = R.drawable.login_image),
            contentDescription = "School Image",
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    text = "Welcome to NPAL2",
                    style = MaterialTheme.typography.headlineSmall
                )


                Spacer(modifier = Modifier.padding(12.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = {username = it},
                    label = { Text(text = "Username")},
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username") }
                )
                Spacer(modifier = Modifier.padding(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {password = it},
                    label = { Text(text = "Password")},
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.padding(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val isValid = validateLogin(context, username, password)
                            if(isValid){
                                onLoginSuccess()
                                /*showBiometricPrompt(activity = activity,
                                    onSuccess = { onLoginSuccess() },
                                    onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                                )*/
                                /*if (activity != null) {
                                    showBiometricPrompt(
                                        activity = activity,
                                        onSuccess = { onLoginSuccess() },
                                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                                    )
                                } else {
                                    Toast.makeText(context, "Unable to access activity for biometric", Toast.LENGTH_SHORT).show()
                                }*/
                            }else{
                                Toast.makeText(context,"Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ){
                    Text(text = "Login")
                }
                Spacer(modifier = Modifier.padding(8.dp))

                Text(text = "New Student?")
                Button(
                    onClick = {
                        if(username.isNotEmpty() && password.isNotEmpty()){
                            scope.launch {
                                val isCreated = performSignUp(context, username, password)
                                if(isCreated){
                                    Toast.makeText(context, "User has been successfully created", Toast.LENGTH_LONG).show()
                                }else{
                                    Toast.makeText(context, "Failed to create user", Toast.LENGTH_LONG).show()
                                }
                            }
                        }else{
                            Toast.makeText(context, "Please provide more details.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                {
                    Text(text = "Register")
                }
            }
        }
    }
}

suspend fun performSignUp(context: Context, username: String, password: String): Boolean{
    //Firebase
    return FirebaseHelper().signUp(username, password)
}
suspend fun validateLogin(context: Context, username: String, password: String): Boolean{
    return FirebaseHelper().signIn(username, password)
}

// Helper that checks device biometric readiness
fun isBiometricAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    val can = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
    return can == BiometricManager.BIOMETRIC_SUCCESS
}

// Function to show BiometricPrompt. Pass the current Activity (must be FragmentActivity or ComponentActivity)
@RequiresApi(Build.VERSION_CODES.P)
fun showBiometricPrompt(
    activity: Activity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor: Executor = ContextCompat.getMainExecutor(activity)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError(errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            // called when biometric doesn't match
            onError("Authentication failed")
        }
    }

    val biometricPrompt = BiometricPrompt(activity as androidx.fragment.app.FragmentActivity, executor, callback)


    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric login")
        .setSubtitle("Use fingerprint or device credential to sign in")
        // either allow device credential (PIN/pattern) OR provide a negative button:
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    biometricPrompt.authenticate(promptInfo)
}