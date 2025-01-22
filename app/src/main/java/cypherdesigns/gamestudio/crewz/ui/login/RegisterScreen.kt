package cypherdesigns.gamestudio.crewz.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun RegisterScreen(
    viewModel: UserViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {

    val context = LocalContext.current
    val userName = rememberSaveable { mutableStateOf("") }
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val verifyPassword = rememberSaveable { mutableStateOf("") }
    val firstName = rememberSaveable { mutableStateOf("") }
    val lastName = rememberSaveable { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isVerifyVisible by rememberSaveable { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Crewz",
                style = MaterialTheme.typography.displayLarge,
                color = colorScheme.primary,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.displaySmall,
                color = colorScheme.primary
            )

            // Input Fields
            OutlinedTextField(
                value = userName.value,
                onValueChange = { userName.value = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            OutlinedTextField(
                value = firstName.value,
                onValueChange = { firstName.value = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lastName.value,
                onValueChange = { lastName.value = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                        painterResource(id = R.drawable.ic_visibility_off)
                    } else {
                        painterResource(id = R.drawable.ic_visibility)
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
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = verifyPassword.value,
                onValueChange = { verifyPassword.value = it },
                label = { Text("Verify Password") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                visualTransformation = if (isVerifyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (isVerifyVisible) {
                        painterResource(id = R.drawable.ic_visibility_off)
                    } else {
                        painterResource(id = R.drawable.ic_visibility)
                    }
                    val description = if (isVerifyVisible) {
                        stringResource(R.string.hide_password)
                    } else {
                        stringResource(R.string.show_password)
                    }

                    IconButton(onClick = { isVerifyVisible = !isVerifyVisible }) {
                        Icon(
                            painter = image,
                            contentDescription = description,
                            tint = colorScheme.primary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Register Button
            Button(
                onClick = {
                    if (userName.value.isBlank()) {
                        Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password.value != verifyPassword.value) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // 1) Check if username is unique in /users.
                    viewModel.checkUsernameUnique(userName.value) { isUnique ->
                        if (isUnique) {
                            // 2) Create user via Firebase Auth
                            auth.createUserWithEmailAndPassword(email.value, password.value)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            // 3) Use ViewModel to populate user fields in /users
                                            //    We set crewId = "" to show they're not assigned to a crew yet.
                                            viewModel.updateUserInfoInUserNode(
                                                crewId = "",
                                                userName = userName.value,
                                                firstName = firstName.value,
                                                lastName = lastName.value,
                                                email = email.value
                                            )
                                            Toast.makeText(
                                                context,
                                                "Account created successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onRegisterSuccess()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Registration Failed: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Username is already taken", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Back to Login Button
            Button(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = buttonColors
            ) {
                Text(text = "Already have an account? Login Here.")
            }
        }
    }
}
