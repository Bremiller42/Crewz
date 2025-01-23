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

    // ---------------------------------------------------------------------
    // READ METHODS
    // ---------------------------------------------------------------------

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
        database.getReference("users/$userId/crewId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.getValue(String::class.java) ?: "")
                }
                override fun onCancelled(error: DatabaseError) {
                    println("getUserCrewId error: ${error.message}")
                }
            })
    }

    // ---------------------------------------------------------------------
    // WRITE METHODS
    // ---------------------------------------------------------------------

    /**
     * A single flexible method for updating any fields in /users/{userId}.
     * This is used internally by any convenience methods, or directly by callers.
     */
    fun updateGlobalUserInfo(
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val userRef = database.getReference("users/$userId")
        userRef.updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown error updating user info for $userId.")
            }
    }

    /**
     * Convenience method to update the "crewId" field for a user in /users/{userId}.
     * Under the hood, calls updateGlobalUserInfo with a single field map.
     */
    fun updateUserCrewId(userId: String, crewId: String) {
        updateGlobalUserInfo(
            userId,
            updates = mapOf("crewId" to crewId),
            onSuccess = {
                println("updateUserCrewId success -> crew=$crewId for user=$userId")
            },
            onFailure = {
                println("updateUserCrewId error: $it")
            }
        )
    }

    /**
     * Convenience method to update userName, firstName, lastName, email, and crewId
     * for a user in /users/{userId}.
     */
    fun updateUserInfoInUserNode(
        userId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String,
        crewId: String
    ) {
        val updates = mapOf(
            "userName" to userName,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "crewId" to crewId
        )
        updateGlobalUserInfo(
            userId,
            updates,
            onSuccess = {
                println("updateUserInfoInUserNode success for $userId")
            },
            onFailure = {
                println("updateUserInfoInUserNode failure: $it")
            }
        )
    }
}
