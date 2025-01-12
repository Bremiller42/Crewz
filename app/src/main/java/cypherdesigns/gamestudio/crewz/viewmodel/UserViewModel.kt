package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel: ViewModel() {
    private val userRepository = UserRepository()
    val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid
    val cachedFirstName: String?
        get() = userRepository.cachedUserFirstName
    val cachedLastName: String?
        get() = userRepository.cachedUserLastName
    val cachedEmail: String?
        get() = userRepository.cachedUserEmail
    val cachedLocationSharingEnabled: Boolean
        get() = userRepository.cachedLocationSharingEnabled

    private val _isLocationSharingEnabled = MutableStateFlow(false)
    val isLocationSharingEnabled = _isLocationSharingEnabled.asStateFlow()

    private val _markerColorHue = MutableStateFlow(0f)
    val markerColorHue = _markerColorHue.asStateFlow()


    fun fetchUserDetails(userId: String) {
        userRepository.fetchAndCacheUserDetails(userId)
    }

    fun observeUserDetails(userId: String) {
        userRepository.observeUserDetails(userId) { isEnabled ->
            _isLocationSharingEnabled.value = isEnabled
        }
    }

    fun observeMarkerColor(userId: String) {
        userRepository.observeMarkerColor(userId) { hue ->
            _markerColorHue.value = hue
        }
    }

    fun updateMarkerColor(userId: String, hue: Float) {
        userRepository.updateMarkerColor(userId, hue)
    }

    fun toggleLocationSharing(userId: String, isEnabled: Boolean) {
        userRepository.updateLocationSharing(userId, isEnabled) { success ->
            if (success) {
                println("Location sharing toggled successfully")
            } else {
                println("Failed to toggle location sharing")
            }
        }
    }

}