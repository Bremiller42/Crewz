package cypherdesigns.gamestudio.crewz.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance()

    var cachedUserFirstName: String? = null
        private set
    var cachedUserLastName: String? = null
        private set
    var cachedUserEmail: String? = null
        private set
    var cachedLocationSharingEnabled: Boolean = false
        private set

    fun observeMarkerColorAndLocationSharing(
        crewId: String, // Include crewId for scoped access
        userId: String,
        onMarkerColorUpdated: (String) -> Unit,
        onLocationSharingUpdated: (Boolean) -> Unit
    ) {
        database.getReference("crews").child(crewId).child("members").child(userId).apply {
            // Observe marker color
            child("markerColor").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val colorName = snapshot.getValue(String::class.java) ?: "red"
                    onMarkerColorUpdated(colorName)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error observing marker color: ${error.message}")
                }
            })

            // Observe location sharing status
            child("locationSharingEnabled").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isLocationSharingEnabled = snapshot.getValue(Boolean::class.java) ?: false
                    onLocationSharingUpdated(isLocationSharingEnabled)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error observing location sharing: ${error.message}")
                }
            })
        }
    }

    fun updateMarkerColor(
        crewId: String, // Include crewId for scoped updates
        userId: String,
        colorName: String,
        isLocationSharingEnabled: Boolean
    ) {
        database.getReference("crews").child(crewId).child("members").child(userId).apply {
            // Update marker color
            child("markerColor").setValue(colorName)
                .addOnSuccessListener {
                    println("Marker color updated to: $colorName")
                }
                .addOnFailureListener { exception ->
                    println("Failed to update marker color: ${exception.message}")
                }

            // Update location sharing status
            child("locationSharingEnabled").setValue(isLocationSharingEnabled)
                .addOnSuccessListener {
                    println("Location sharing status updated to: $isLocationSharingEnabled")
                }
                .addOnFailureListener { exception ->
                    println("Failed to update location sharing: ${exception.message}")
                }
        }
    }

    fun fetchAndCacheUserDetails(userId: String) {
        println("Fetching user details for userId: $userId")
        database.getReference("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    cachedUserFirstName = snapshot.child("firstName").getValue(String::class.java)
                    cachedUserLastName = snapshot.child("lastName").getValue(String::class.java)
                    cachedUserEmail = snapshot.child("email").getValue(String::class.java)
                    println("Cached user details for $userId: FirstName=$cachedUserFirstName")
                } else {
                    println("No user details found for userId: $userId")
                }
            }
            .addOnFailureListener {
                println("Failed to fetch user details: ${it.message}")
            }
    }

    fun observeUserDetails(crewId: String, userId: String, onDetailsUpdated: (Boolean) -> Unit) {
        database.getReference("crews").child(crewId).child("members").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isLocationSharingEnabled =
                        snapshot.child("locationSharingEnabled").getValue(Boolean::class.java) ?: false
                    cachedLocationSharingEnabled = isLocationSharingEnabled
                    onDetailsUpdated(isLocationSharingEnabled)
                    println("Real-time update: LocationSharingEnabled=$isLocationSharingEnabled for $userId")
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error observing user details: ${error.message}")
                }
            })
    }

    fun updateLocationSharing(
        crewId: String,
        userId: String,
        isEnabled: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        database.getReference("crews").child(crewId).child("members").child(userId).child("locationSharingEnabled")
            .setValue(isEnabled)
            .addOnSuccessListener {
                cachedLocationSharingEnabled = isEnabled
                println("Location Sharing updated successfully: $isEnabled")
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                println("Failed to update location sharing: ${exception.message}")
                onComplete(false)
            }
    }

    fun getUserCrewId(userId: String, onResult: (String) -> Unit) {
        database.getReference("users").child(userId).child("crewId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.getValue(String::class.java) ?: "")
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Failed to fetch crew Id: ${error.message}")
                }
            })
    }

    fun getUserCrewName(crewId: String, onResult: (String) -> Unit) {
        database.getReference("crews").child(crewId).child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.getValue(String::class.java) ?: "")
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Failed to fetch crew Id: ${error.message}")
                }
            })
    }

    fun updateUserCrewId(userId: String, crewId: String) {
        // Update global user data
        database.getReference("users").child(userId).child("crewId").setValue(crewId)
            .addOnSuccessListener { println("Crew ID updated successfully for userId: $userId") }
            .addOnFailureListener { println("Error updating Crew ID: ${it.message}") }
    }

    fun updateUserDetails(
        userId: String,
        crewId: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        // Update user details in the crew's members node
        database.getReference("crews").child(crewId).child("members").child(userId).setValue(
            mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "markerColor" to "red", // Default value
                "locationSharingEnabled" to false // Default value
            )
        ).addOnSuccessListener {
            println("User details successfully added to crew $crewId")
        }.addOnFailureListener {
            println("Error adding user details to crew $crewId: ${it.message}")
        }

        // Update global user data
        database.getReference("users").child(userId).setValue(
            mapOf(
                "crewId" to crewId,
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email
            )
        ).addOnSuccessListener {
            println("Global user details updated successfully for userId: $userId")
        }.addOnFailureListener {
            println("Error updating global user details: ${it.message}")
        }
    }

}
