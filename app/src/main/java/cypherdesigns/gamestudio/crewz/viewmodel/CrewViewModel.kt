package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cypherdesigns.gamestudio.crewz.data.dataclasses.CrewInfo
import cypherdesigns.gamestudio.crewz.data.repository.CrewRepository
import cypherdesigns.gamestudio.crewz.ui.map.MemberLocation
import cypherdesigns.gamestudio.crewz.ui.memberlist.CrewMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrewViewModel : ViewModel() {

    private val crewRepository = CrewRepository()

    private val _crewMembers = MutableStateFlow<List<CrewMember>>(emptyList())
    val crewMembers: StateFlow<List<CrewMember>> = _crewMembers.asStateFlow()

    private val _currentCrewName = MutableStateFlow<String?>(null)
    val currentCrewName: StateFlow<String?> = _currentCrewName.asStateFlow()

    private val _currentCrewRole = MutableStateFlow<String?>(null)
    val currentCrewRole: StateFlow<String?> = _currentCrewRole.asStateFlow()

    private val _isLocationSharingEnabled = MutableStateFlow(false)
    val isLocationSharingEnabled: StateFlow<Boolean> = _isLocationSharingEnabled.asStateFlow()

    private val _markerColorName = MutableStateFlow("red")
    val markerColorName: StateFlow<String> = _markerColorName.asStateFlow()

    // NEW: A list of crews available to join
    private val _availableCrews = MutableStateFlow<List<CrewInfo>>(emptyList())
    val availableCrews: StateFlow<List<CrewInfo>> = _availableCrews.asStateFlow()

    private val _crewLocations = MutableStateFlow<List<MemberLocation>>(emptyList())
    val crewLocations: StateFlow<List<MemberLocation>> = _crewLocations.asStateFlow()

    // ----------------------------------------------------
    // Observing All Crews
    // ----------------------------------------------------
    fun observeAllCrews() {
        // This function calls a new method in CrewRepository that listens
        // to the entire "crews" node and returns a list of CrewInfo(id,name).
        crewRepository.observeAllCrews { crewList ->
            _availableCrews.value = crewList
        }
    }

    // ----------------------------------------------------
    // Creating a new crew
    // ----------------------------------------------------
    /**
     * Create a new crew in Firebase, set "ownerId" to the given user,
     * and return the newly generated crewId to the callback.
     */
    fun createCrew(
        crewName: String,
        ownerUserId: String,
        ownerUserName: String,
        ownerFirstName: String,
        ownerLastName: String,
        ownerEmail: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        crewRepository.createCrew(
            crewName = crewName,
            ownerUserId = ownerUserId,
            ownerUserName = ownerUserName,
            ownerFirstName = ownerFirstName,
            ownerLastName = ownerLastName,
            ownerEmail = ownerEmail,
            onSuccess = { newCrewId ->
                onSuccess(newCrewId)
            },
            onFailure = { error ->
                onFailure(error)
            }
        )
    }

    // ----------------------------------------------------
    // Crew Membership
    // ----------------------------------------------------

    fun observeCrewMembers(crewId: String) {
        crewRepository.observeCrewMembers(crewId) { members ->
            _crewMembers.value = members
        }
    }

    fun cleanupCrewMembersListener(crewId: String) {
        crewRepository.cleanupCrewMembersListener(crewId)
    }

    // ----------------------------------------------------
    // Crew Name
    // ----------------------------------------------------

    fun fetchCrewName(crewId: String) {
        crewRepository.getCrewName(crewId) { name ->
            _currentCrewName.value = name
        }
    }

    // ----------------------------------------------------
    // Marker color & location sharing
    // ----------------------------------------------------

    /**
     * Listen for changes to markerColor and locationSharingEnabled in real-time.
     */
    fun observeMarkerColorAndLocationSharing(crewId: String, userId: String) {
        crewRepository.observeMarkerColorAndLocationSharing(
            crewId,
            userId,
            onMarkerColorUpdated = { color ->
                _markerColorName.value = color
            },
            onLocationSharingUpdated = { isEnabled ->
                _isLocationSharingEnabled.value = isEnabled
            }
        )
    }

    /**
     * Observe user details in the crew node (we specifically use it to track locationSharingEnabled).
     */
    fun observeUserDetails(crewId: String, userId: String) {
        crewRepository.observeUserDetails(crewId, userId) { enabled ->
            _isLocationSharingEnabled.value = enabled
        }
    }

    /**
     * Example convenience function to change the marker color.
     * Internally calls the unified updateCrewUserInfo(...) with a Map of fields to update.
     */
    fun updateMarkerColor(crewId: String, userId: String, colorName: String) {
        // We can also preserve the user's current location sharing setting
        // or pass in separate arguments, whichever you prefer.
        updateCrewUserInfo(
            crewId = crewId,
            userId = userId,
            updates = mapOf(
                "markerColor" to colorName,
                // Optionally also update locationSharingEnabled at the same time
                "locationSharingEnabled" to _isLocationSharingEnabled.value
            )
        )
    }

    /**
     * Toggle location sharing by building the appropriate Map
     * and then calling updateCrewUserInfo.
     */
    fun toggleLocationSharing(crewId: String, userId: String, isEnabled: Boolean) {
        // Notice we no longer call crewRepository.updateLocationSharing,
        // we just rely on updateCrewUserInfo with a single field
        updateCrewUserInfo(
            crewId = crewId,
            userId = userId,
            updates = mapOf("locationSharingEnabled" to isEnabled),
            onSuccess = {
                // If the update was successful, reflect in local state
                _isLocationSharingEnabled.value = isEnabled
            },
            onFailure = {
                println("toggleLocationSharing failed: $it")
            }
        )
    }

    // ----------------------------------------------------
    // Online / Offline
    // ----------------------------------------------------

    fun updateOnlineStatus(crewId: String, userId: String, isOnline: Boolean) {
        crewRepository.updateOnlineStatus(crewId, userId, isOnline)
    }

    fun observeConnectionStatus(crewId: String, userId: String) {
        crewRepository.observeConnectionStatus(crewId, userId)
    }


    /**
     * A single method for updating arbitrary fields in /crews/{crewId}/members/{userId}.
     */
    fun updateCrewUserInfo(
        crewId: String,
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            crewRepository.updateCrewUserInfo(
                crewId = crewId,
                userId = userId,
                updates = updates,
                onSuccess = onSuccess,
                onFailure = onFailure
            )
        }
    }

    fun observeCrewLocations(crewId: String) {
        crewRepository.observeCrewLocations(
            crewId = crewId,
            onLocations = { rawList ->
                // Convert each map to a MemberLocation
                val newList = rawList.mapNotNull { mapData ->
                    val userName = mapData["userName"] as? String ?: return@mapNotNull null
                    val markerColor = mapData["markerColor"] as? String ?: "red"
                    val lat = mapData["latitude"] as? Double ?: 0.0
                    val lng = mapData["longitude"] as? Double ?: 0.0
                    MemberLocation(userName, lat, lng, markerColor)
                }
                _crewLocations.value = newList
            },
            onError = { error ->
                println("observeCrewLocations error: $error")
                // Optionally handle errors
            }
        )
    }

    fun createOrUpdateCrewMember(
        crewId: String,
        userId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String,
        role: String = "member"
    ) {
        // Build all fields
        val newMemberFields = mapOf(
            "userName" to userName,
            "firstName" to firstName,
            "lastName"  to lastName,
            "email"     to email,
            "role"      to role,
            "markerColor" to "red",
            "locationSharingEnabled" to false
        )
        // Then just call updateCrewUserInfo
        updateCrewUserInfo(crewId, userId, newMemberFields)
    }

    fun observeMemberRole(crewId: String, userId: String) {
        crewRepository.observeMemberRole(crewId, userId) { role ->
            _currentCrewRole.value = role
        }
    }
    fun getMemberRoleOnce(
        crewId: String,
        userId: String,
        onComplete: (String?) -> Unit
    ) {
        crewRepository.getMemberRoleOnce(crewId, userId) { existingRole ->
            onComplete(existingRole)
        }
    }

    fun setMemberRole(crewId: String, userId: String, role: String) {
        crewRepository.setMemberRole(
            crewId, userId, role,
            onSuccess = {
                println("Role updated to $role for user=$userId")
            },
            onFailure = {
                println("Failed to update role: $it")
            }
        )
    }

}
