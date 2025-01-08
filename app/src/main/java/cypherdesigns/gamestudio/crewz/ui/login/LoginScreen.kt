package cypherdesigns.gamestudio.crewz.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.viewmodel.ChatViewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cypherdesigns.gamestudio.crewz.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation
import cypherdesigns.gamestudio.crewz.data.credentials.getCredentials
import cypherdesigns.gamestudio.crewz.data.credentials.saveCredentials
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import androidx.compose.ui.text.font.FontStyle
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    var isPasswordVisible by remember { mutableStateOf(false) }

    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = colorScheme.secondary,
        unfocusedBorderColor = colorScheme.primary,
        containerColor = colorScheme.background,
        focusedLabelColor = colorScheme.primary)

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = colorScheme.primary,
        contentColor = Color.Black
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Crewz",
                style = MaterialTheme.typography.displayLarge,
                color = colorScheme.primary,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (isPasswordVisible) {
                        painterResource(id = R.drawable.ic_visibility_off) // Icon for visible
                    } else {
                        painterResource(id = R.drawable.ic_visibility) // Icon for hidden
                    }

                    val description = if (isPasswordVisible) {
                        stringResource(R.string.hide_password)
                    } else {
                        stringResource(R.string.show_password)
                    }

                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(painter = image, contentDescription = description, tint = colorScheme.primary)
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(email.value, password.value)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                                viewModel.fetchUserDetails(userId) // Call the function from ChatViewModel
                                saveCredentials(context, email.value, password.value)
                                onLoginSuccess()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Login Failed: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                colors = buttonColors,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Login")
            }

            Button(
                onClick = {
                    showBiometricPrompt(
                        context as FragmentActivity,
                        onLoginSuccess = {
                            Toast.makeText(context, "Authentication Succeeded", Toast.LENGTH_SHORT)
                                .show()
                            // Navigate to the next screen or perform any post-login actions
                            onLoginSuccess()
                        },
                        onLoginFailure = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        },
                        viewModel = viewModel
                    )
                },
                colors = buttonColors,
                modifier = Modifier.fillMaxWidth()

            ) {
                Text("Biometric Login")
            }


            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateToRegister,
                colors = buttonColors,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Signup for a Crewz Account Here.")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun showBiometricPrompt(
    activity: FragmentActivity,
    onLoginSuccess: () -> Unit,
    onLoginFailure: (String) -> Unit,
    viewModel: UserViewModel
) {
    // Executor for handling the prompt's callback
    val executor: Executor = activity.mainExecutor

    // Callback to handle authentication events
    val biometricPromptCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)

            // Retrieve stored credentials
            val (email, password) = getCredentials(activity)

            if (email != null && password != null) {
                // Use FirebaseAuth to sign in the user
                val auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                viewModel.fetchUserDetails(userId) // Fetch user details
                            }
                            onLoginSuccess()
                        } else {
                            onLoginFailure("Firebase login failed: ${task.exception?.message}")
                        }
                    }
            } else {
                onLoginFailure("No saved credentials found.")
            }
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onLoginFailure("Authentication failed")
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onLoginFailure("Authentication error: $errString")
        }
    }

    // Create the BiometricPrompt instance
    val biometricPrompt = BiometricPrompt(activity, executor, biometricPromptCallback)

    // Create the prompt info
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Login")
        .setSubtitle("Log in using biometric credentials")
        .setNegativeButtonText("Cancel")
        .build()

    // Show the prompt
    biometricPrompt.authenticate(promptInfo)
}