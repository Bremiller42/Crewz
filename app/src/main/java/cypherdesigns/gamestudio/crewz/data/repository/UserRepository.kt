package cypherdesigns.gamestudio.crewz.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("userLocations")

    var cachedUserFirstName: String? = null
        private set
    var cachedUserLastName: String? = null
        private set
    var cachedUserEmail: String? = null
        private set
    var cachedLocationSharingEnabled: Boolean = false
        private set

    fun observeMarkerColorAndLocationSharing(
        userId: String,
        onMarkerColorUpdated: (String) -> Unit,
        onLocationSharingUpdated: (Boolean) -> Unit
    ) {
        database.child(userId).apply {
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


    fun updateMarkerColor(userId: String, colorName: String, isLocationSharingEnabled: Boolean) {
        database.child(userId).apply {
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
        println("Fetching user details from Firestore for userId: $userId")
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    println("Document Snapshot: ${documentSnapshot.data}")

                    cachedUserFirstName = documentSnapshot.getString("firstName")
                    cachedUserLastName = documentSnapshot.getString("lastName")
                    cachedUserEmail = documentSnapshot.getString("email")
                    cachedLocationSharingEnabled =
                        documentSnapshot.getBoolean("isLocationSharingEnabled") ?: false
                    println("Cached user first name: $cachedUserFirstName")
                    println("Cached user last name: $cachedUserLastName")
                    println("Cached user email: $cachedUserEmail")
                    println("Cached user location sharing: $cachedLocationSharingEnabled")

                } else {
                    println("No user details found in Firestore for userId: $userId")
                }
            }
            .addOnFailureListener {
                println("Failed to fetch user details from Firestore: ${it.message}")
            }
    }

    fun observeUserDetails(userId: String, onDetailsUpdated: (Boolean) -> Unit) {
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error observing user details: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val isLocationSharingEnabled =
                        snapshot.getBoolean("isLocationSharingEnabled") ?: false
                    cachedLocationSharingEnabled = isLocationSharingEnabled
                    onDetailsUpdated(isLocationSharingEnabled)
                    println("Real-time update: isLocationSharingEnabled = $isLocationSharingEnabled")
                }
            }
    }

    fun updateLocationSharing(userId: String, isEnabled: Boolean, onComplete: (Boolean) -> Unit) {
        val updates = mapOf("isLocationSharingEnabled" to isEnabled)

        firestore.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                cachedLocationSharingEnabled = isEnabled
                println("Location Sharing updated successfully: $isEnabled")
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                println("Failed to update location sharing: ${exception.message}")
                onComplete(false)
            }

        database.child(userId).child("locationSharingEnabled").setValue(isEnabled)
            .addOnSuccessListener {
                println("Location sharing updated to: $isEnabled")
            }
            .addOnFailureListener { exception ->
                println("Failed to update location sharing: ${exception.message}")
            }
    }
    fun observeUserLocationSharing(userId: String, onLocationSharingUpdated: (Boolean) -> Unit) {
        database.child(userId).child("locationSharingEnabled")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isLocationSharingEnabled = snapshot.getValue(Boolean::class.java) ?: false
                    onLocationSharingUpdated(isLocationSharingEnabled)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Failed to observe location sharing: ${error.message}")
                }
            })
    }

}

