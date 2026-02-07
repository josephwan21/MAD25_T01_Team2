package np.ict.mad.mad25_t01_team2_npal2

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun SettingsScreen(
    onAccountClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingsItem(
                title = "Account",
                subtitle = "Email, password",
                onClick = onAccountClick
            )
        }

        item {
            SettingsItem(
                title = "Notifications",
                subtitle = "Manage notifications",
                onClick = onNotificationsClick
            )
        }

        item {
            SettingsItem(
                title = "Help & Support",
                subtitle = "FAQ, contact us",
                onClick = onHelpClick
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log out")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    var enabled by rememberSaveable { mutableStateOf(isNotificationsEnabled(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable notifications")
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        setNotificationsEnabled(context, it)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    firebaseHelper: FirebaseHelper,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    var email by remember { mutableStateOf(user?.email ?: "") }

    // Password fields
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    var newUsername by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Fetch profile once
    LaunchedEffect(Unit) {
        userProfile = firebaseHelper.getUserProfile()
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Account") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(16.dp)) {

            // Display current username and student ID
            userProfile?.let { profile ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Username: ${profile.username}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Student ID: ${profile.studentId}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Update Username ---
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("New Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (newUsername.isNotBlank()) {
                        loading = true
                        scope.launch {
                            val success = firebaseHelper.updateUsername(newUsername)
                            if (success) {
                                Toast.makeText(context, "Username updated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to update username", Toast.LENGTH_LONG).show()
                            }
                            loading = false
                        }
                    } else {
                        Toast.makeText(context, "Please enter new username", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Username")
            }

            Spacer(Modifier.height(24.dp))

            // --- Current password for re-authentication ---
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // --- Update Password ---
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                        Toast.makeText(context, "Please fill all password fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    loading = true
                    scope.launch {
                        val success = firebaseHelper.updatePassword(newPassword, currentPassword)
                        if (success) {
                            Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                            newPassword = ""
                            confirmPassword = ""
                            currentPassword = ""
                        } else {
                            Toast.makeText(context, "Failed to update password", Toast.LENGTH_LONG).show()
                        }
                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Password")
            }

            if (loading) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Help & Support") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Frequently Asked Questions", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Q: How do I create a task?\n" +
                        "A: Go to Tasks tab, click Add Task, and fill in the details.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Q: How do I update my account info?\n" +
                        "A: Go to Settings → Account to change email or password.",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Text("Contact Support", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Email: support@npal2.edu\nPhone: +65 1234 5678",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    // Optionally launch email intent
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
                    intent.type = "message/rfc822"
                    intent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("support@npal2.edu"))
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "NPAL2 App Support")
                    context.startActivity(
                        android.content.Intent.createChooser(intent, "Send Email")
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact via Email")
            }
        }
    }
}


@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
