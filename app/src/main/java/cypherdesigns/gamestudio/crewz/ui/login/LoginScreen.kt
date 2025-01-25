// LoginScreen.kt
package cypherdesigns.gamestudio.crewz.ui.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.data.credentials.getCredentials
import cypherdesigns.gamestudio.crewz.data.credentials.saveCredentials
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.fragment.app.FragmentActivity
import cypherdesigns.gamestudio.crewz.R
import java.util.concurrent.Executor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = MaterialTheme.colorScheme.secondary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.secondary
    )

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.Black
    )

    // Show biometric prompt if credentials exist
    LaunchedEffect(Unit) {
        val (storedEmail, storedPassword) = getCredentials(context)
        if (!storedEmail.isNullOrEmpty() && !storedPassword.isNullOrEmpty()) {
            delay(500)
            showBiometricPrompt(
                activity = context as FragmentActivity,
                onLoginSuccess = { onLoginSuccess() },
                onLoginFailure = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                },
                viewModel = viewModel,
                context = context
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                color = MaterialTheme.colorScheme.primary,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
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
                    val image = if (isPasswordVisible)
                        painterResource(id = R.drawable.ic_visibility_off)
                    else painterResource(id = R.drawable.ic_visibility)
                    val desc = if (isPasswordVisible)
                        stringResource(R.string.hide_password)
                    else stringResource(R.string.show_password)

                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(image, contentDescription = desc, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        loginUser(
                            auth, viewModel,
                            email.value, password.value, context,
                            onLoginSuccess = { onLoginSuccess() },
                            onLoginFailure = { e -> Toast.makeText(context, e, Toast.LENGTH_LONG).show() }
                        )
                    }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    loginUser(
                        auth, viewModel,
                        email.value, password.value, context,
                        onLoginSuccess = { onLoginSuccess() },
                        onLoginFailure = { e -> Toast.makeText(context, e, Toast.LENGTH_LONG).show() }
                    )
                },
                colors = buttonColors,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    showBiometricPrompt(
                        activity = context as FragmentActivity,
                        onLoginSuccess = { onLoginSuccess() },
                        onLoginFailure = { e ->
                            Toast.makeText(context, e, Toast.LENGTH_SHORT).show()
                        },
                        viewModel = viewModel,
                        context = context
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
                Text("Signup for a Crewz Account Here.")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun showBiometricPrompt(
    activity: FragmentActivity,
    onLoginSuccess: () -> Unit,
    onLoginFailure: (String) -> Unit,
    viewModel: UserViewModel,
    context: Context
) {
    val executor: Executor = activity.mainExecutor
    val (storedEmail, storedPassword) = getCredentials(context)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            if (!storedEmail.isNullOrEmpty() && !storedPassword.isNullOrEmpty()) {
                loginUser(
                    FirebaseAuth.getInstance(),
                    viewModel,
                    storedEmail, storedPassword, context,
                    onLoginSuccess = { onLoginSuccess() },
                    onLoginFailure = { e -> onLoginFailure(e) }
                )
            } else {
                onLoginFailure("No saved credentials found.")
            }
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onLoginFailure("Authentication error: $errString")
        }
        override fun onAuthenticationFailed() {
            onLoginFailure("Authentication failed.")
        }
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Login")
        .setSubtitle("Log in as $storedEmail")
        .setNegativeButtonText("Cancel")
        .build()

    BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
}

fun loginUser(
    auth: FirebaseAuth,
    userViewModel: UserViewModel,
    email: String,
    password: String,
    context: Context,
    onLoginSuccess: () -> Unit,
    onLoginFailure: (String) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Save credentials locally
                saveCredentials(context, email, password)
                Toast.makeText(context, "Login Credentials Saved...", Toast.LENGTH_SHORT).show()

                // 1) Retrieve the newly logged-in user’s UID
                val currentUid = auth.currentUser?.uid
                if (currentUid == null) {
                    onLoginFailure("Login failed: currentUid is null after signIn.")
                    return@addOnCompleteListener
                }

                // 2) Let the UserViewModel know the user is logged in:
                userViewModel.setUserId(currentUid)

                // 3) Now you can safely fetchCrewId etc.
                userViewModel.fetchCrewId(currentUid) {
                    onLoginSuccess()
                }
                userViewModel.fetchUserDetails(currentUid)
            } else {
                onLoginFailure("Login Failed: ${task.exception?.message}")
            }
        }

}
