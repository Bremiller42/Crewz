package cypherdesigns.gamestudio.crewz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cypherdesigns.gamestudio.crewz.data.ImageData
import cypherdesigns.gamestudio.crewz.data.repository.GalleryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {
    private val galleryRepository = GalleryRepository()

    private val _imageUrls = MutableStateFlow<List<ImageData>>(emptyList())
    val imageUrls: StateFlow<List<ImageData>> = _imageUrls.asStateFlow()

    /**
     * Fetch images for a specific crew.
     */
    fun fetchImages(crewId: String) {
        viewModelScope.launch {
            galleryRepository.fetchImages(crewId) { images ->
                _imageUrls.value = images
            }
        }
    }

    /**
     * Upload an image to a specific crew's gallery.
     */
    fun uploadImage(crewId: String, imageUrl: String, uploadedBy: String) {
        viewModelScope.launch {
            galleryRepository.uploadImage(crewId, imageUrl, uploadedBy)
            // Optionally refresh the gallery after upload
            fetchImages(crewId)
        }
    }
}
