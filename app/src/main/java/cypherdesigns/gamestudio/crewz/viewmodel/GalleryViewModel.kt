package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import cypherdesigns.gamestudio.crewz.data.ImageData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val _imageUrls = MutableStateFlow<List<ImageData>>(emptyList())
    val imageUrls: StateFlow<List<ImageData>> = _imageUrls.asStateFlow()

    fun fetchImageUrls() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("images")
        databaseReference.get()
            .addOnSuccessListener { snapshot ->
                val imageList = mutableListOf<ImageData>()
                snapshot.children.forEach { child ->
                    val url = child.child("url").getValue(String::class.java) ?: ""
                    val uploadedBy = child.child("uploadedBy").getValue(String::class.java) ?: "Unknown"
                    imageList.add(ImageData(url, uploadedBy))
                }
                _imageUrls.value = imageList // Assuming _imageUrls is a MutableStateFlow in your ViewModel
            }
            .addOnFailureListener {
                println("Failed to fetch images: ${it.message}")
            }
    }

}