package cypherdesigns.gamestudio.crewz.data.repository

import com.google.firebase.database.*
import cypherdesigns.gamestudio.crewz.data.dataclasses.CrewInfo
import cypherdesigns.gamestudio.crewz.ui.memberlist.CrewMember

class CrewRepository {

    private val database = FirebaseDatabase.getInstance()
    private val crewListeners = mutableMapOf<String, ValueEventListener>()

    // -------------------------------------------
    // Observe ALL Crews in Realtime
    // -------------------------------------------
    fun observeAllCrews(onResult: (List<CrewInfo>) -> Unit) {
        val crewsRef = database.getReference("crews")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val crewList = snapshot.children.mapNotNull { crewSnap ->
                    val crewId = crewSnap.key
                    val name = crewSnap.child("name").getValue(String::class.java)
                    if (crewId != null && name != null) {
                        CrewInfo(id = crewId, name = name)
                    } else null
                }
                onResult(crewList)
            }
            override fun onCancelled(error: DatabaseError) {
                println("observeAllCrews error: ${error.message}")
                onResult(emptyList())
            }
        }
        crewsRef.addValueEventListener(listener)

    }

    // -------------------------------------------
    // Create a new crew node in "crews"
    // -------------------------------------------
    fun createCrew(
        crewName: String,
        ownerId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val crewsRef = database.getReference("crews")
        val newCrewId = crewsRef.push().key
        if (newCrewId == null) {
            onFailure("Failed to generate unique crewId")
            return
        }
        val data = mapOf(
            "name" to crewName,
            "ownerId" to ownerId
            // You can add other default fields here if needed
        )
        crewsRef.child(newCrewId).setValue(data)
            .addOnSuccessListener {
                onSuccess(newCrewId)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown error creating crew.")
            }
    }

    /**
     * Observe real-time marker color & location for a user in a crew.
     * This is purely a "read" convenience method.
     */
    fun observeMarkerColorAndLocationSharing(
        crewId: String,
        userId: String,
        onMarkerColorUpdated: (String) -> Unit,
        onLocationSharingUpdated: (Boolean) -> Unit
    ) {
        val memberRef = database.getReference("crews/$crewId/members/$userId")

        // Marker color
        memberRef.child("markerColor")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val colorName = snapshot.getValue(String::class.java) ?: "red"
                    onMarkerColorUpdated(colorName)
                }
                override fun onCancelled(error: DatabaseError) {
                    println("observeMarkerColor error: ${error.message}")
                }
            })

        // Location sharing
        memberRef.child("locationSharingEnabled")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val enabled = snapshot.getValue(Boolean::class.java) ?: false
                    onLocationSharingUpdated(enabled)
                }
                override fun onCancelled(error: DatabaseError) {
                    println("observeLocationSharing error: ${error.message}")
                }
            })
    }

    /**
     * A single flexible method for updating user fields in /crews/{crewId}/members/{userId}.
     *
     * E.g. updateCrewUserInfo("crew123", "user789", mapOf("markerColor" to "blue", "online" to true), {...}, {...})
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
                onFailure(e.message ?: "Unknown error while updating /crews/$crewId/members/$userId")
            }
    }

    /**
     * Observe whether a user is locationSharingEnabled, or any other fields you like.
     * (Kept separate because it's purely a read loop.)
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
     * Observe all members in a crew in real-time.
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
     * Fetch the crew name from /crews/{crewId}/name (single read).
     */
    fun getCrewName(crewId: String, onResult: (String) -> Unit) {
        val nameRef = database.getReference("crews/$crewId/name")
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
     * Mark a user online/offline in the crew node.
     * Usually used in combination with .info/connected logic.
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
     * Listen to .info/connected to automatically set user online or offline.
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
     * A helper method to add a brand-new crew member with default fields
     * (if you still want a single-call "create" approach).
     */
    fun createOrUpdateCrewMember(
        crewId: String,
        userId: String,
        userName: String,
        firstName: String,
        lastName: String,
        email: String
    ) {
        val newMemberFields = mapOf(
            "userName" to userName,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "markerColor" to "red",
            "locationSharingEnabled" to false
        )

        updateCrewUserInfo(
            crewId = crewId,
            userId = userId,
            updates = newMemberFields,
            onSuccess = {
                println("createOrUpdateCrewMember success for user=$userId in crew=$crewId")
            },
            onFailure = {
                println("Error: $it")
            }
        )
    }

    fun observeCrewLocations(
        crewId: String,
        onLocations: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        val membersRef = FirebaseDatabase.getInstance().getReference("crews/$crewId/members")

        // Optionally store the listener if you need to remove it later
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // We can return the raw data or parse it into your "MemberLocation" model if you like
                val newLocations = mutableListOf<Map<String, Any>>()
                snapshot.children.forEach { child ->
                    val locationSharing = child.child("locationSharingEnabled")
                        .getValue(Boolean::class.java) ?: false
                    if (locationSharing) {
                        val mapData = mutableMapOf<String, Any>()
                        mapData["userName"] = child.child("userName").getValue(String::class.java) ?: "Unknown"
                        mapData["markerColor"] = child.child("markerColor").getValue(String::class.java) ?: "red"
                        mapData["latitude"] = child.child("latitude").getValue(Double::class.java) ?: 0.0
                        mapData["longitude"] = child.child("longitude").getValue(Double::class.java) ?: 0.0
                        newLocations.add(mapData)
                    }
                }
                onLocations(newLocations)
            }
            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        membersRef.addValueEventListener(listener)
        // If you want to remove it later, store it in a Map<crewId, listener> etc.
    }
}
