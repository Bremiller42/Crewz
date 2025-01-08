package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class UserViewModel: ViewModel() {
    var cachedUserFirstName: String? = null
        private set
    var cachedUserLastName: String? = null
        private set
    var cachedUserEmail: String? = null
        private set

    var isLocationSharingEnabled by mutableStateOf(false)
        private set

    fun fetchAndCacheUserDetails(userId: String) {
        println("Fetching user details from Firestore for userId: $userId")
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    println("Document Snapshot: ${documentSnapshot.data}")

                    val firstName = documentSnapshot.getString("firstName")
                    val lastName = documentSnapshot.getString("lastName")
                    val userEmail = documentSnapshot.getString("email")

                    cachedUserFirstName = firstName
                    println("Cached user first name: $cachedUserFirstName")

                    cachedUserLastName = lastName
                    println("Cached user last name: $cachedUserLastName")

                    cachedUserEmail = userEmail
                    println("Cached user email: $cachedUserEmail")

                    println("GitCheck")
                } else {
                    println("No user details found in Firestore for userId: $userId")
                }
            }
            .addOnFailureListener {
                println("Failed to fetch user details from Firestore: ${it.message}")
            }
    }

    fun toggleLocationSharing() {
        isLocationSharingEnabled = !isLocationSharingEnabled
    }

}