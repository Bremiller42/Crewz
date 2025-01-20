package cypherdesigns.gamestudio.crewz.data.repository

import com.google.firebase.database.*
import cypherdesigns.gamestudio.crewz.ui.memberlist.CrewMember

class CrewRepository {

    private val database = FirebaseDatabase.getInstance()
    private val crewListeners = mutableMapOf<String, ValueEventListener>()

    /**
     * Observe real-time marker color & location for a user in a crew
     */
    fun observeMarkerColorAndLocationSharing(
        crewId: String,
        userId: String,
        onMarkerColorUpdated: (String) -> Unit,
        onLocationSharingUpdated: (Boolean) -> Unit
    ) {
        val memberRef = database.getReference("crews")
            .child(crewId)
            .child("members")
            .child(userId)

        // Marker color
        memberRef.child("markerColor").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val colorName = snapshot.getValue(String::class.java) ?: "red"
                onMarkerColorUpdated(colorName)
            }
            override fun onCancelled(error: DatabaseError) {
                println("observeMarkerColor: ${error.message}")
            }
        })

        // Location sharing
        memberRef.child("locationSharingEnabled").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val enabled = snapshot.getValue(Boolean::class.java) ?: false
                onLocationSharingUpdated(enabled)
            }
            override fun onCancelled(error: DatabaseError) {
                println("observeLocationSharing: ${error.message}")
            }
        })
    }

    /**
     * Update the marker color and location sharing for a user in a crew
     */
    fun updateMarkerColor(
        crewId: String,
        userId: String,
        colorName: String,
        isLocationSharingEnabled: Boolean
    ) {
        val memberRef = database.getReference("crews")
            .child(crewId)
            .child("members")
            .child(userId)

        memberRef.child("markerColor").setValue(colorName)
            .addOnSuccessListener {
                println("Marker color updated to: $colorName")
            }
            .addOnFailureListener { e ->
                println("Failed to update marker color: ${e.message}")
            }

        memberRef.child("locationSharingEnabled").setValue(isLocationSharingEnabled)
            .addOnSuccessListener {
                println("Location sharing updated to $isLocationSharingEnabled")
            }
            .addOnFailureListener { e ->
                println("Failed to update location sharing: ${e.message}")
            }
    }

    /**
     * Observe the locationSharingEnabled in a crew for a user
     */
    fun observeUserDetails(
        crewId: String,
        userId: String,
        onDetailsUpdated: (Boolean) -> Unit
    ) {
        val ref = database.getReference("crews/$crewId/members/$userId")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isEnabled = snapshot.child("locationSharingEnabled")
                    .getValue(Boolean::class.java) ?: false
                onDetailsUpdated(isEnabled)
            }
            override fun onCancelled(error: DatabaseError) {
                println("observeUserDetailsInCrew error: ${error.message}")
            }
        })
    }

    /**
     * Enable/Disable location sharing
     */
    fun updateLocationSharing(
        crewId: String,
        userId: String,
        isEnabled: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        val ref = database.getReference("crews/$crewId/members/$userId/locationSharingEnabled")
        ref.setValue(isEnabled)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener {
                println("updateLocationSharing failure: ${it.message}")
                onComplete(false)
            }
    }

    /**
     * Fetch the crew name
     */
    fun getCrewName(crewId: String, onResult: (String) -> Unit) {
        val nameRef = database.getReference("crews").child(crewId).child("name")
        nameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onResult(snapshot.getValue(String::class.java) ?: "")
            }
            override fun onCancelled(error: DatabaseError) {
                println("getCrewName error: ${error.message}")
                onResult("")
            }
        })
    }

    /**
     * Insert/update user details in the crew's members node: /crews/{crewId}/members/{userId}
     */
    fun updateCrewMemberInCrewNode(
        crewId: String,
        userId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        val ref = database.getReference("crews/$crewId/members/$userId")
        val data = mapOf(
            "userName" to userName,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "markerColor" to "red",
            "locationSharingEnabled" to false
        )
        ref.setValue(data)
            .addOnSuccessListener {
                println("updateCrewMemberInCrewNode: success for user $userId in crew $crewId")
            }
            .addOnFailureListener {
                println("Error updating crew member: ${it.message}")
            }
    }

    /**
     * Observe all members in a crew
     */
    fun observeCrewMembers(crewId: String, onResult: (List<CrewMember>) -> Unit) {
        if (crewId.isEmpty()) {
            onResult(emptyList())
            return
        }
        val crewRef = database.getReference("crews/$crewId/members")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val members = snapshot.children.mapNotNull { child ->
                    val name = child.child("userName").getValue(String::class.java) ?: "Unknown"
                    val online = child.child("online").getValue(Boolean::class.java) ?: false
                    CrewMember(name, online)
                }
                onResult(members)
            }
            override fun onCancelled(error: DatabaseError) {
                println("observeCrewMembers error: ${error.message}")
                onResult(emptyList())
            }
        }
        crewRef.addValueEventListener(listener)
        crewListeners[crewId] = listener
    }

    fun cleanupCrewMembersListener(crewId: String) {
        crewListeners[crewId]?.let { listener ->
            database.getReference("crews/$crewId/members").removeEventListener(listener)
        }
        crewListeners.remove(crewId)
    }

    /**
     * Update user online/offline in crew
     */
    fun updateOnlineStatus(crewId: String, userId: String, isOnline: Boolean) {
        val ref = database.getReference("crews/$crewId/members/$userId/online")
        ref.setValue(isOnline)
            .addOnSuccessListener {
                println("updateOnlineStatus success: $userId -> $isOnline")
            }
            .addOnFailureListener {
                println("updateOnlineStatus error: ${it.message}")
            }
        if (isOnline) {
            ref.onDisconnect().setValue(false)
        }
    }

    /**
     * Observe .info/connected to set user online automatically
     */
    fun observeConnectionStatus(crewId: String, userId: String) {
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
                    updateOnlineStatus(crewId, userId, true)
                } else {
                    println("observeConnectionStatus: user $userId in crew $crewId disconnected")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("observeConnectionStatus error: ${error.message}")
            }
        })
    }

    /**
     * Update fields in /crews/{crewId}/members/{userId}
     */
    fun updateCrewUserInfo(
        crewId: String,
        userId: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ref = database.getReference("crews/$crewId/members/$userId")
        ref.updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown error updating crew user info.")
            }
    }
}
