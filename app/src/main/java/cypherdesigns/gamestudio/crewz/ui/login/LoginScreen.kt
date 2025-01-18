package cypherdesigns.gamestudio.crewz.ui.login

import android.content.Context
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
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    onLoginSuccess: (String?) -> Unit, // Pass `crewId` to decide next navigation
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colorScheme.primary,
        unfocusedTextColor = colorScheme.secondary,
        focusedLabelColor = colorScheme.primary,
        unfocusedLabelColor = colorScheme.secondary,
        focusedContainerColor = colorScheme.background,
        unfocusedContainerColor = colorScheme.background,
        focusedTrailingIconColor = colorScheme.primary,
        unfocusedLeadingIconColor = colorScheme.primary,
        focusedBorderColor = colorScheme.primary,
        unfocusedBorderColor = colorScheme.secondary
    )

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = colorScheme.primary,
        contentColor = Color.Black
    )

    // Automatically show biometric prompt if credentials are available
    LaunchedEffect(Unit) {
        val (storedEmail, storedPassword) = getCredentials(context)
        if (!storedEmail.isNullOrEmpty() && !storedPassword.isNullOrEmpty()) {
            // Delay to ensure UI is rendered before showing prompt
            kotlinx.coroutines.delay(500)
            showBiometricPrompt(
                context as FragmentActivity,
                onLoginSuccess = onLoginSuccess,
                onLoginFailure = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                },
                viewModel = viewModel,
                context = context
            )
        }
    }


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
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next // Show "Next" on the keyboard
                ),
                keyboardActions = KeyboardActions(
                    onNext = { /* Move focus to the next field */ focusManager.moveFocus(
                        FocusDirection.Down
                    )
                    }
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
                        Icon(
                            painter = image,
                            contentDescription = description,
                            tint = colorScheme.primary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { loginUser(
                        auth = auth,
                        viewModel = viewModel,
                        email = email.value,
                        password = password.value,
                        context = context,
                        onLoginSuccess = onLoginSuccess,
                        onLoginFailure = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    ) }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    loginUser(
                        auth = auth,
                        viewModel = viewModel,
                        email = email.value,
                        password = password.value,
                        context = context,
                        onLoginSuccess = onLoginSuccess,
                        onLoginFailure = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                colors = buttonColors,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    showBiometricPrompt(
                        context as FragmentActivity,
                        onLoginSuccess = onLoginSuccess,
                        onLoginFailure = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
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
                Text(text = "Signup for a Crewz Account Here.")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun showBiometricPrompt(
    activity: FragmentActivity,
    onLoginSuccess: (String?) -> Unit,
    onLoginFailure: (String) -> Unit,
    viewModel: UserViewModel,
    context: Context
) {

    val executor: Executor = activity.mainExecutor
    val (storedEmail, storedPassword) = getCredentials(context)

    val biometricPromptCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            if (!storedEmail.isNullOrEmpty() && !storedPassword.isNullOrEmpty()) {
                loginUser(
                    auth = FirebaseAuth.getInstance(),
                    viewModel = viewModel,
                    email = storedEmail,
                    password = storedPassword,
                    context = context,
                    onLoginSuccess = onLoginSuccess,
                    onLoginFailure = onLoginFailure
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

    BiometricPrompt(activity, executor, biometricPromptCallback).authenticate(promptInfo)
}

fun loginUser(
    auth: FirebaseAuth,
    viewModel: UserViewModel,
    email: String,
    password: String,
    context: Context,
    onLoginSuccess: (String?) -> Unit,
    onLoginFailure: (String) -> Unit
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Save credentials locally for future biometric login
                saveCredentials(context, email, password)

                Toast.makeText(
                    context,
                    "Login Credentials Saved. You can now use biometrics.",
                    Toast.LENGTH_SHORT
                ).show()

                val userId = viewModel.currentUserId
                viewModel.fetchCrewId(userId!!) { crewId ->
                    if (!crewId.isNullOrEmpty()) {
                        onLoginSuccess(crewId) // Navigate to home or another screen
                    } else {
                        onLoginSuccess(null)
                    }
                }
                viewModel.fetchUserDetails(userId)
            } else {
                onLoginFailure("Login Failed: ${task.exception?.message}")
            }
        }
}