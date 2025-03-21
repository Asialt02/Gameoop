@file:Suppress("DEPRECATION")

package com.example.programmeringskurstilmorilddataba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.programmeringskurstilmorilddataba.ui.theme.ProgrammeringskursTilMorildDataBATheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            ProgrammeringskursTilMorildDataBATheme {
                AppNavigation()
            }
        }
    }
}
//Comment Amir


@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val loginRoute = "Login"
    val registerRoute = "Register"
    val adminCourseRoute = "adminCourse"
    val userUIRoute = "userUI"
    val userProfileRoute = "userProfile"

    NavHost(
        navController = navController,
        startDestination = loginRoute
    ) {
        composable(loginRoute) {
            LoginScreen(navController)
        }
        composable(registerRoute) {
            RegisterScreen(navController)
        }
        composable(adminCourseRoute) {
            AdminCourseScreen(navController)
        }
        composable(userUIRoute) {
            UserUIScreen(navController)
        }
        composable("courseScreen/{courseName}") { backStackEntry ->
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            CourseScreen(navController, courseName)
        }
        composable(userProfileRoute) {
            UserProfile(navController)
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var errorMessage by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        db.collection("users").document(it.uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val isAdmin = document.getBoolean("isAdmin") ?: false
                                    if (isAdmin) {
                                        navController.navigate("adminCourse")
                                    } else {
                                        navController.navigate("userUI")
                                    }
                                } else {
                                    errorMessage = "User data not found."
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = "Failed to fetch user data: ${e.message}"
                            }
                    }
                } else {
                    errorMessage = "Login failed: ${task.exception?.message}"
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gameoop!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Login Text
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { loginUser(email.text, password.text) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log in")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Forgot password?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                showForgotPasswordDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // No Account? Join Now! Text
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "No account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Join now!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Forgot Password") },
            text = {
                Column {
                    Text("Enter your email address to receive a password reset link.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (email.text.isNotBlank()) {
                            sendPasswordResetEmail(email.text) { success, message ->
                                if (success) {
                                    errorMessage = message
                                    showForgotPasswordDialog = false
                                } else {
                                    errorMessage = message
                                }
                            }
                        } else {
                            errorMessage = "Please enter your email address."
                        }
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showForgotPasswordDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    var fullName by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var errorMessage by remember { mutableStateOf("") }
    val invalidEmailMessage = "Invalid Email!"

    fun registerUser(fullName: String, email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = fullName
                    }

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            val userData = hashMapOf(
                                "name" to fullName,
                                "email" to email,
                                "isAdmin" to false,
                                "password" to password
                            )

                            db.collection("users").document(user.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    errorMessage = "Registration successful!"
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "Failed to save user data: ${e.message}"
                                }
                        } else {
                            errorMessage = "Failed to update profile: ${profileTask.exception?.message}"
                        }
                    }
                } else {
                    errorMessage = "Registration failed: ${task.exception?.message}"
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gameoop! Text
        Text(
            text = "Gameoop!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Register Text
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Username Input
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Register Button
        Button(
            onClick = {
                checkEmailValidity(email.text) { isValid ->
                    if (isValid) {
                        registerUser(fullName.text, email.text, password.text)
                    } else {
                        errorMessage = invalidEmailMessage
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Return to Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Terms of Service Text
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Terms of Service? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Read more",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                }
            )
        }

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Dashboard", "userUI"),
        BottomNavItem("Courses", "Courses"),
        BottomNavItem("Profile", "userProfile"),
        BottomNavItem("Friends", "Friends"),
        BottomNavItem("Settings", "Settings")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00008B))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach { item ->
            Text(
                text = item.title,
                color = Color.White,
                modifier = Modifier
                    .clickable {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                    .padding(8.dp)
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String
)

fun checkEmailValidity(email: String, onResult: (Boolean) -> Unit) {
    val apiKey = "4a3503e6c3244594a00dd486d77126ac"
    val url = "https://emailvalidation.abstractapi.com/v1/?api_key=$apiKey&email=$email"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(false)
        }


        override fun onResponse(call: Call, response: Response) {
            response.use { res ->
                if (!res.isSuccessful) {
                    onResult(false)
                    return
                }

                val responseData = res.body?.string()
                val jsonObject = JSONObject(responseData ?: "{}")

                val deliverability = jsonObject.optString("deliverability", "UNDELIVERABLE")

                onResult(deliverability == "DELIVERABLE")
            }
        }
    })
}

fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, "Password reset email sent to $email")
            } else {
                onResult(false, task.exception?.message ?: "Failed to send reset email")
            }
        }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    ProgrammeringskursTilMorildDataBATheme {
        AppNavigation()
    }
}