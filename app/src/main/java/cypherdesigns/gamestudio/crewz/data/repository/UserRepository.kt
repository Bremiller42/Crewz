package cypherdesigns.gamestudio.crewz.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository {

    private val database = FirebaseDatabase.getInstance()

    var cachedUserName: String? = null
        private set
    var cachedUserFirstName: String? = null
        private set
    var cachedUserLastName: String? = null
        private set
    var cachedUserEmail: String? = null
        private set

    /**
     * Check if userName is unique across /users
     */
    fun isUsernameUnique(userName: String, onResult: (Boolean) -> Unit) {
        database.getReference("users")
            .orderByChild("userName")
            .equalTo(userName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(!snapshot.exists()) // True if no node found
                }
                override fun onCancelled(error: DatabaseError) {
                    println("isUsernameUnique: error: ${error.message}")
                    onResult(false)
                }
            })
    }

    /**
     * Fetch user details from /users/{userId} and cache them.
     */
    fun fetchAndCacheUserDetails(userId: String) {
        database.getReference("users")
            .child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    cachedUserName = snapshot.child("userName").getValue(String::class.java)
                    cachedUserFirstName = snapshot.child("firstName").getValue(String::class.java)
                    cachedUserLastName = snapshot.child("lastName").getValue(String::class.java)
                    cachedUserEmail = snapshot.child("email").getValue(String::class.java)
                } else {
                    println("No user details found for $userId")
                }
            }
            .addOnFailureListener {
                println("fetchAndCacheUserDetails failed: ${it.message}")
            }
    }

    /**
     * Return crewId from /users/{userId}/crewId
     */
    fun getUserCrewId(userId: String, onResult: (String) -> Unit) {
        database.getReference("users")
            .child(userId)
            .child("crewId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.getValue(String::class.java) ?: "")
                }
                override fun onCancelled(error: DatabaseError) {
                    println("getUserCrewId error: ${error.message}")
                }
            })
    }

    /**
     * Update /users/{userId}/crewId
     */
    fun updateUserCrewId(userId: String, crewId: String) {
        database.getReference("users")
            .child(userId)
            .child("crewId")
            .setValue(crewId)
            .addOnSuccessListener {
                println("updateUserCrewId success -> crew=$crewId for user=$userId")
            }
            .addOnFailureListener {
                println("updateUserCrewId error: ${it.message}")
            }
    }

    /**
     * Update /users/{userId} with new user fields
     */
    fun updateUserInfoInUserNode(
        userId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String,
        crewId: String
    ) {
        val ref = database.getReference("users").child(userId)
        val updates = mapOf(
            "userName" to userName,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "crewId" to crewId
        )
        ref.setValue(updates)
            .addOnSuccessListener {
                println("updateUserInfoInUserNode success for $userId")
            }
            .addOnFailureListener {
                println("updateUserInfoInUserNode failure: ${it.message}")
            }
    }

    /**
     * Update arbitrary fields in /users/{userId}
     */
    fun updateGlobalUserInfo(
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userRef = database.getReference("users/$userId")
        userRef.updateChildren(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown error updating user info.")
            }
    }
}
