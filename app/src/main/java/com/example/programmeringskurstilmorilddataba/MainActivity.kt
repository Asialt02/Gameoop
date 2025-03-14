@file:Suppress("DEPRECATION")

package com.example.programmeringskurstilmorilddataba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.programmeringskurstilmorilddataba.ui.theme.ProgrammeringskursTilMorildDataBATheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import com.google.firebase.FirebaseApp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.ktx.userProfileChangeRequest
import okhttp3.*
import org.json.JSONObject
import java.io.IOException



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            ProgrammeringskursTilMorildDataBATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    
    val loginRoute = stringResource(R.string.login)
    val registerRoute = stringResource(R.string.register)

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
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var errorMessage by remember { mutableStateOf("") }

    val loginCompletedMessage = stringResource(R.string.login_completed)
    val loginFailedMessage = stringResource(R.string.login_failed)
    val registerMessage = stringResource(R.string.register)

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    errorMessage = loginCompletedMessage
                } else {
                    errorMessage = loginFailedMessage + task.exception?.message
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
        Text(stringResource(R.string.login),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.background(Color.LightGray, shape = RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.e_mail)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { loginUser(email.text, password.text) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.log_in))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate(registerMessage) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.go_to_registration))
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()

    var fullName by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var errorMessage by remember { mutableStateOf("") }

    val registrationCompletedMessage = stringResource(R.string.registration_completed)
    val registrationFailedMessage = stringResource(R.string.registration_failed)
    val invalidEmailMessage = "Invalid Email!"

    fun registerUser(fullName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = fullName
                    }
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        errorMessage = if (profileTask.isSuccessful) registrationCompletedMessage
                        else registrationFailedMessage + profileTask.exception?.message
                    }
                } else {
                    errorMessage = registrationFailedMessage + task.exception?.message
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
        Text(stringResource(R.string.register),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.background(Color.LightGray, shape = RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text(stringResource(R.string.full_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.e_mail)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            Text(stringResource(R.string.register))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.return_to_login))
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

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



@Preview(showBackground = true)
@Composable
fun Preview() {
    ProgrammeringskursTilMorildDataBATheme {
        AppNavigation()
    }
}