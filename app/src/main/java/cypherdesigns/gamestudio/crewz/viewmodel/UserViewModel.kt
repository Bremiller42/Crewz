package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()

    val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    // The user's crewId from /users/{userId}/crewId
    private val _currentCrewId = MutableStateFlow<String?>(null)
    val currentCrewId: StateFlow<String?> = _currentCrewId.asStateFlow()

    val cachedUserName: String?
        get() = userRepository.cachedUserName
    val cachedFirstName: String?
        get() = userRepository.cachedUserFirstName
    val cachedLastName: String?
        get() = userRepository.cachedUserLastName
    val cachedEmail: String?
        get() = userRepository.cachedUserEmail

    /**
     * Fetch and cache user details from /users/{userId}.
     */
    fun fetchUserDetails(userId: String) {
        userRepository.fetchAndCacheUserDetails(userId)
    }

    /**
     * Check if a username is unique (for registration).
     */
    fun checkUsernameUnique(userName: String, onResult: (Boolean) -> Unit) {
        userRepository.isUsernameUnique(userName, onResult)
    }

    /**
     * Fetch the user's crewId from /users/{userId}/crewId
     */
    fun fetchCrewId(userId: String, onComplete: (String?) -> Unit) {
        userRepository.getUserCrewId(userId) { crewId ->
            _currentCrewId.value = if (crewId.isEmpty()) null else crewId
            onComplete(_currentCrewId.value)
        }
    }

    /**
     * Update the user's crewId in /users/{userId}.
     */
    fun updateCrewId(crewId: String) {
        val userId = currentUserId ?: return
        userRepository.updateUserCrewId(userId, crewId)
        _currentCrewId.value = crewId
    }

    /**
     * Update user info in the /users/{userId} node
     */
    fun updateUserInfoInUserNode(
        crewId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        val userId = currentUserId ?: return
        userRepository.updateUserInfoInUserNode(
            userId,
            userName,
            firstName,
            lastName,
            email,
            crewId
        )
    }

    /**
     * Generic updates to /users/{userId}
     */
    fun updateGlobalUserInfo(
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val userId = currentUserId ?: return onFailure("User ID is null")
        userRepository.updateGlobalUserInfo(userId, updates, onSuccess, onFailure)
    }
}
