package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.data.repository.UserRepository
import cypherdesigns.gamestudio.crewz.ui.memberlist.CrewMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val _currentCrewId = MutableStateFlow<String?>(null)
    val currentCrewId = _currentCrewId.asStateFlow()

    private val _currentCrewName = MutableStateFlow<String?>(null)
    val currentCrewName = _currentCrewName.asStateFlow()

    val cachedUserName: String?
        get() = userRepository.cachedUserName
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

    private val _crewMembers = MutableStateFlow<List<CrewMember>>(emptyList())
    val crewMembers: StateFlow<List<CrewMember>> = _crewMembers.asStateFlow()

    /**
     * Fetch and cache user details for the specified crew and user.
     */
    fun fetchUserDetails(userId: String) {
        userRepository.fetchAndCacheUserDetails(userId)
    }

    /**
     * Observe user details for the specified crew and user and update state.
     */
    fun observeUserDetails(crewId: String, userId: String) {
        userRepository.observeUserDetails(crewId, userId) { isEnabled ->
            _isLocationSharingEnabled.value = isEnabled
        }
    }

    /**
     * Observe marker color and location sharing status in real-time for the specified crew and user.
     */
    fun observeMarkerColorAndLocationSharing(crewId: String, userId: String) {
        userRepository.observeMarkerColorAndLocationSharing(
            crewId,
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
     * Update marker color in Realtime Database for the specified crew and user.
     */
    fun updateMarkerColor(crewId: String, userId: String, colorName: String) {
        userRepository.updateMarkerColor(crewId, userId, colorName, _isLocationSharingEnabled.value)
    }

    /**
     * Toggle location sharing for the specified crew and user and update state.
     */
    fun toggleLocationSharing(crewId: String, userId: String, isEnabled: Boolean) {
        userRepository.updateLocationSharing(crewId, userId, isEnabled) { success ->
            if (success) {
                _isLocationSharingEnabled.value = isEnabled
                println("Location sharing toggled successfully")
            } else {
                println("Failed to toggle location sharing")
            }
        }
    }

    /**
     * Fetch the crew ID for the current user.
     */
    fun fetchCrewId(userId: String, onComplete: (String?) -> Unit) {
        userRepository.getUserCrewId(userId) { crewId ->
            _currentCrewId.value = if (crewId.isNotEmpty()) crewId else null
            onComplete(_currentCrewId.value) // Pass the crewId to the callback
        }
    }


    fun fetchCrewName(crewId: String) {
        userRepository.getUserCrewName(crewId) { crewName ->
            _currentCrewName.value = crewName
        }
    }


    fun updateCrewId(crewId: String) {
        val userId = currentUserId ?: return

        // Update the user's crew ID in the global "users" node
        userRepository.updateUserCrewId(userId, crewId)

        // Update the local state
        _currentCrewId.value = crewId
    }

    fun updateUserInformation(
        crewId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        val userId = currentUserId ?: return

        // Update detailed user info in the crew's member list
        userRepository.updateUserDetails(crewId, userId, userName, firstName, lastName, email)

        // Update local state
        _currentCrewId.value = crewId
        println("Updated $userId details: \n CrewId: $crewId \n UserName: $userName \n Name: $firstName $lastName \n Email: $email")
    }
    fun updateGlobalUserInfo(
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = { println("Global Update Error: $it") }
    ) {
        val userId = currentUserId ?: return onFailure("User ID is null")
        userRepository.updateGlobalUserInfo(userId, updates, onSuccess, onFailure)
    }

    fun updateCrewUserInfo(
        crewId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = { println("Crew Update Error: $it") }
    ) {
        val userId = currentUserId ?: return onFailure("User ID is null")
        userRepository.updateCrewUserInfo(crewId, userId, updates, onSuccess, onFailure)
    }

    fun observeCrewMembers(crewId: String) {
        userRepository.observeCrewMembers(crewId) { members ->
            _crewMembers.value = members
        }
    }

    fun updateOnlineStatus(crewId: String, userId: String, isOnline: Boolean) {
        userRepository.updateOnlineStatus(crewId, userId, isOnline)
    }

    fun observeConnectionStatus(crewId: String, userId: String) {
        userRepository.observeConnectionStatus(crewId, userId)
    }

    fun checkUsernameUnique(userName: String, onResult: (Boolean) -> Unit) {
        userRepository.isUsernameUnique(userName, onResult)
    }


}
