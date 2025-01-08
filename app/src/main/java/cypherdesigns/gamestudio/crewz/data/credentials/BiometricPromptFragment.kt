package cypherdesigns.gamestudio.crewz.data.credentials

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class BiometricPromptFragment : Fragment() {
    fun showBiometricPrompt(callback: (Boolean) -> Unit) {
        val biometricPrompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(requireContext()),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    callback(true)
                }

                override fun onAuthenticationFailed() {
                    callback(false)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    callback(false)
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Authenticate using biometrics")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}