package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cypherdesigns.gamestudio.crewz.data.repository.CrewRepository
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

    private val _isLocationSharingEnabled = MutableStateFlow(false)
    val isLocationSharingEnabled: StateFlow<Boolean> = _isLocationSharingEnabled.asStateFlow()

    private val _markerColorName = MutableStateFlow("red")
    val markerColorName: StateFlow<String> = _markerColorName.asStateFlow()

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

    fun observeUserDetails(crewId: String, userId: String) {
        crewRepository.observeUserDetails(crewId, userId) { enabled ->
            _isLocationSharingEnabled.value = enabled
        }
    }

    fun updateMarkerColor(crewId: String, userId: String, colorName: String) {
        crewRepository.updateMarkerColor(
            crewId,
            userId,
            colorName,
            _isLocationSharingEnabled.value
        )
    }

    fun toggleLocationSharing(crewId: String, userId: String, isEnabled: Boolean) {
        crewRepository.updateLocationSharing(crewId, userId, isEnabled) { success ->
            if (success) {
                _isLocationSharingEnabled.value = isEnabled
            }
        }
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

    // ----------------------------------------------------
    // Update user data in the "crews" node
    // ----------------------------------------------------

    fun updateCrewMemberInCrewNode(
        crewId: String,
        userId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        crewRepository.updateCrewMemberInCrewNode(
            crewId, userId, userName, firstName, lastName, email
        )
    }

    fun updateCrewUserInfo(
        crewId: String,
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            crewRepository.updateCrewUserInfo(
                crewId, userId, updates, onSuccess, onFailure
            )
        }
    }
}
