// data.utilities.AppLifecycleObserver.kt
package cypherdesigns.gamestudio.crewz.data.utilities

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.database.FirebaseDatabase

class AppLifecycleObserver(
    private val crewId: String,
    private val userId: String,
    private val onStatusChange: (Boolean) -> Unit
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        updateOnlineStatus(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        updateOnlineStatus(false)
    }

    private fun updateOnlineStatus(isOnline: Boolean) {
        val userRef = FirebaseDatabase.getInstance()
            .getReference("crews/$crewId/members/$userId/online")
        userRef.setValue(isOnline).addOnSuccessListener {
            onStatusChange(isOnline)
        }.addOnFailureListener {
            println("Failed to update online status: ${it.message}")
        }
        if (isOnline) {
            userRef.onDisconnect().setValue(false)
        }
    }
}
