package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cypherdesigns.gamestudio.crewz.data.repository.LocationData
import cypherdesigns.gamestudio.crewz.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val locationRepository = LocationRepository()

    private val _userLocations = MutableStateFlow<Map<String, LocationData>>(emptyMap())
    val userLocations: StateFlow<Map<String, LocationData>> = _userLocations

    fun updateUserLocation(userId: String, latitude: Double, longitude: Double, isEnabled: Boolean) {
        locationRepository.updateUserLocation(userId, latitude, longitude, isEnabled)
    }

    fun observeUserLocations() {
        locationRepository.observeLocations { locations ->
            viewModelScope.launch {
                _userLocations.value = locations
            }
        }
    }
}