package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {
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

    // State for location sharing
    private val _isLocationSharingEnabled = MutableStateFlow(false)
    val isLocationSharingEnabled = _isLocationSharingEnabled.asStateFlow()

    // State for marker color
    private val _markerColorName = MutableStateFlow("red") // Default to red
    val markerColorName = _markerColorName.asStateFlow()

    /**
     * Fetch and cache user details from Firestore.
     */
    fun fetchUserDetails(userId: String) {
        userRepository.fetchAndCacheUserDetails(userId)
    }

    /**
     * Observe user details from Firestore and update state.
     */
    fun observeUserDetails(userId: String) {
        userRepository.observeUserDetails(userId) { isEnabled ->
            _isLocationSharingEnabled.value = isEnabled
        }
    }

    /**
     * Observe marker color and location sharing status in real-time.
     */
    fun observeMarkerColorAndLocationSharing(userId: String) {
        userRepository.observeMarkerColorAndLocationSharing(
            userId,
            onMarkerColorUpdated = { colorName ->
                _markerColorName.value = colorName
            },
            onLocationSharingUpdated = { isEnabled ->
                _isLocationSharingEnabled.value = isEnabled
            }
        )
    }

    /**
     * Update marker color in Realtime Database.
     */
    fun updateMarkerColor(userId: String, colorName: String) {
        userRepository.updateMarkerColor(userId, colorName, _isLocationSharingEnabled.value)
    }

    /**
     * Toggle location sharing and update state.
     */
    fun toggleLocationSharing(userId: String, isEnabled: Boolean) {
        userRepository.updateLocationSharing(userId, isEnabled) { success ->
            if (success) {
                _isLocationSharingEnabled.value = isEnabled
                println("Location sharing toggled successfully")
            } else {
                println("Failed to toggle location sharing")
            }
        }
    }
}
