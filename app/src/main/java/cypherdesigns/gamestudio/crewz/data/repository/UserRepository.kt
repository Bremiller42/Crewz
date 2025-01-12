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

    fun observeMarkerColor(userId: String, onColorUpdated: (String) -> Unit) {
        database.child(userId).child("markerColor")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val colorName = snapshot.getValue(String::class.java) ?: "red"
                    onColorUpdated(colorName)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error observing marker color: ${error.message}")
                }
            })
    }


    fun updateMarkerColor(userId: String, colorName: String) {
        database.child(userId).child("markerColor").setValue(colorName)
            .addOnSuccessListener {
                println("Marker color updated to: $colorName")
            }
            .addOnFailureListener { exception ->
                println("Failed to update marker color: ${exception.message}")
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
                    cachedLocationSharingEnabled = documentSnapshot.getBoolean("isLocationSharingEnabled") ?: false
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
                if (error != null){
                    println("Error observing user details: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val isLocationSharingEnabled = snapshot.getBoolean("isLocationSharingEnabled") ?: false
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
    }

}