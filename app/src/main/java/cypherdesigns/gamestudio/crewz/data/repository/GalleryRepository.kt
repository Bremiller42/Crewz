// GalleryRepository.kt
package cypherdesigns.gamestudio.crewz.data.repository

import com.google.firebase.database.FirebaseDatabase
import cypherdesigns.gamestudio.crewz.data.ImageData

class GalleryRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun uploadImage(crewId: String, imageUrl: String, uploadedBy: String) {
        val galleryRef = database.getReference().child("crews")
            .child(crewId).child("gallery")

        galleryRef.push().setValue(
            mapOf(
                "url" to imageUrl,
                "uploadedBy" to uploadedBy,
                "timestamp" to System.currentTimeMillis()
            )
        ).addOnSuccessListener {
            println("Image uploaded successfully: $imageUrl")
        }.addOnFailureListener {
            println("Error uploading image: ${it.message}")
        }
    }

    fun fetchImages(crewId: String, onResult: (List<ImageData>) -> Unit) {
        val galleryRef = database.getReference().child("crews")
            .child(crewId).child("gallery")

        galleryRef.get()
            .addOnSuccessListener { snapshot ->
                val images = snapshot.children.mapNotNull { child ->
                    val url = child.child("url").getValue(String::class.java)
                    val uploadedBy = child.child("uploadedBy").getValue(String::class.java) ?: "Unknown"
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                    if (url != null) ImageData(url, uploadedBy, timestamp) else null
                }
                onResult(images)
            }
            .addOnFailureListener {
                println("Failed to fetch images: ${it.message}")
            }
    }
}
