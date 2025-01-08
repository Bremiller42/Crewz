package cypherdesigns.gamestudio.crewz.data.repository

import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    val firestore = FirebaseFirestore.getInstance()

    var cachedUserFirstName: String? = null
        private set
    var cachedUserLastName: String? = null
        private set
    var cachedUserEmail: String? = null
        private set
    var cachedLocationSharingEnabled: Boolean = false
        private set

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