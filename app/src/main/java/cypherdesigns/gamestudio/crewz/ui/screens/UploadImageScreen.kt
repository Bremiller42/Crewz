package cypherdesigns.gamestudio.crewz.ui.screens

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.storage.StorageReference
import cypherdesigns.gamestudio.crewz.viewmodel.GalleryViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun UploadImageScreen(
    storageReference: StorageReference,
    galleryViewModel: GalleryViewModel,
    userViewModel: UserViewModel,
    onNavigateToGalleryScreen: () -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.Black
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Launch image picker automatically
    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Selected Image",
                modifier = Modifier.height(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val uploadedUrl = uploadImage(storageReference, uri)
                        val uploaderName = userViewModel.cachedFirstName ?: "Unknown User"
                        if (uploadedUrl != null) {
                            saveImageUrlToDatabase(
                                uploadedUrl,
                                uploaderName,
                                galleryViewModel,
                                userViewModel
                            )
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT).show()
                                onNavigateToGalleryScreen()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                colors = buttonColors
            ) {
                Text("Upload Image")
            }
        } ?: Text("No image selected", style = MaterialTheme.typography.bodyMedium)
    }
}

suspend fun uploadImage(storageReference: StorageReference, uri: Uri): String? {
    return try {
        val fileName = uri.lastPathSegment ?: "image_${System.currentTimeMillis()}"
        val imageRef = storageReference.child("images/$fileName")

        imageRef.putFile(uri).await()
        imageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        println("Failed to upload image: ${e.message}")
        null
    }
}

fun saveImageUrlToDatabase(
    url: String,
    uploaderName: String,
    galleryViewModel: GalleryViewModel,
    userViewModel: UserViewModel
) {
    val crewId = userViewModel.currentCrewId.value ?: return
    galleryViewModel.uploadImage(crewId, url, uploaderName)
}
