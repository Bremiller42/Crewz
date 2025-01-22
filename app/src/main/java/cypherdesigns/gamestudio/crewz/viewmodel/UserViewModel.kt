package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // The user's crewId from /users/{userId}/crewId
    private val _currentCrewId = MutableStateFlow<String?>(null)
    val currentCrewId: StateFlow<String?> = _currentCrewId.asStateFlow()

    // Cached user data
    val cachedUserName: String? get() = userRepository.cachedUserName
    val cachedFirstName: String? get() = userRepository.cachedUserFirstName
    val cachedLastName:  String? get() = userRepository.cachedUserLastName
    val cachedEmail:     String? get() = userRepository.cachedUserEmail

    // -------------------------------------------------
    // Reading user info
    // -------------------------------------------------
    fun fetchUserDetails(userId: String) {
        userRepository.fetchAndCacheUserDetails(userId)
    }

    fun checkUsernameUnique(userName: String, onResult: (Boolean) -> Unit) {
        userRepository.isUsernameUnique(userName, onResult)
    }

    fun fetchCrewId(userId: String, onComplete: (String?) -> Unit) {
        userRepository.getUserCrewId(userId) { crewId ->
            _currentCrewId.value = if (crewId.isEmpty()) null else crewId
            onComplete(_currentCrewId.value)
        }
    }

    // -------------------------------------------------
    // Writing user info
    // -------------------------------------------------

    fun setUserId(uid: String?) {
        _currentUserId.value = uid
    }

    /**
     * Convenience method to update only the 'crewId' field in /users/{userId}.
     * Internally calls updateGlobalUserInfo(...) with a single field.
     */
    fun updateCrewId(crewId: String) {
        val userId = currentUserId ?: return
        updateGlobalUserInfo(
            updates = mapOf("crewId" to crewId),
            onSuccess = {
                println("Updated crewId to $crewId for user=$userId")
                _currentCrewId.value = crewId
            },
            onFailure = {
                println("updateCrewId error: $it")
            }
        )
    }

    /**
     * Convenience method to update userName, firstName, lastName, email, and crewId in /users/{userId}.
     * Internally calls updateGlobalUserInfo(...) with those fields.
     */
    fun updateUserInfoInUserNode(
        crewId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        val userId = currentUserId ?: return
        val updates = mapOf(
            "userName" to userName,
            "firstName" to firstName,
            "lastName"  to lastName,
            "email"     to email,
            "crewId"    to crewId
        )
        updateGlobalUserInfo(
            updates = updates,
            onSuccess = {
                println("updateUserInfoInUserNode success for $userId")
            },
            onFailure = {
                println("updateUserInfoInUserNode failure: $it")
            }
        )
    }

    /**
     * The universal method for updating any fields in /users/{userId}.
     * All specialized convenience methods call this internally.
     */
    fun updateGlobalUserInfo(
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val uid = _currentUserId.value
            ?: return onFailure("No user ID set in ViewModel")

        userRepository.updateGlobalUserInfo(
            userId = uid,
            updates = updates,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}
