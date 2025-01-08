package cypherdesigns.gamestudio.crewz.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LocationRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val locationsRef: DatabaseReference = database.getReference("users")

    fun updateUserLocation(userId: String, latitude: Double, longitude: Double, isEnabled: Boolean) {
        val locationData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "isLocationEnabled" to isEnabled
        )
        locationsRef.child(userId).setValue(locationData)
            .addOnSuccessListener {
                println("Location updated successfully")
            }
            .addOnFailureListener {
                println("Failed to update location: ${it.message}")
            }
    }

    fun observeLocations(onLocationsChanged: (Map<String, LocationData>) -> Unit) {
        locationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = snapshot.children.mapNotNull { child ->
                    val userId = child.key
                    val latitude = child.child("latitude").getValue(Double::class.java)
                    val longitude = child.child("longitude").getValue(Double::class.java)
                    val isEnabled = child.child("isLocationSharingEnabled").getValue(Boolean::class.java) ?: false
                    if (userId != null && latitude != null && longitude != null && isEnabled) {
                        userId to LocationData(latitude, longitude)
                    } else null
                }.toMap()
                onLocationsChanged(locations)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to fetch locations: ${error.message}")
            }
        })
    }
}

data class LocationData(val latitude: Double, val longitude: Double)