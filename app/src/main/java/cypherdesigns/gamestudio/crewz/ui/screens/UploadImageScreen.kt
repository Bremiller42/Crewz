package cypherdesigns.gamestudio.crewz.ui.screens

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import cypherdesigns.gamestudio.crewz.viewmodel.GalleryViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.ChatViewModel

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
    chatViewModel: ChatViewModel,
    onNavigateToGalleryScreen: () -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = colorScheme.primary,
        contentColor = Color.Black
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val permission = android.Manifest.permission.READ_MEDIA_IMAGES
            permissionLauncher.launch(permission)
            launcher.launch("image/*")
        }) {
            Text(text = "Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        val uploaderName = chatViewModel.cachedUserFirstName ?: "Unknown User"
                        if (uploadedUrl != null) {
                            saveImageUrlToDatabase(uploadedUrl, uploaderName)
                            galleryViewModel.fetchImageUrls()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Image Uploaded", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    onNavigateToGalleryScreen()
                },
                colors = buttonColors
            ) {
                Text(text = "Upload Image")
            }
        }
    }
}

suspend fun uploadImage(storageReference: StorageReference, uri: Uri): String? {
    return try {
        val fileName = uri.lastPathSegment ?: "image_${System.currentTimeMillis()}"
        val imageRef = storageReference.child("images/$fileName")

        // Upload image to storage
        imageRef.putFile(uri).await()

        // Get the download URL
        imageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        println("Failed to upload image: ${e.message}")
        null
    }
}

fun saveImageUrlToDatabase(url: String, uploaderName: String) {
    val databaseReference = FirebaseDatabase.getInstance().getReference("images")
    val key = databaseReference.push().key ?: return
    val imageData = mapOf(
        "url" to url,
        "uploadedBy" to uploaderName
    )
    databaseReference.child(key).setValue(imageData)
        .addOnSuccessListener {
            println("Image URL saved to database successfully.")
        }
        .addOnFailureListener {
            println("Failed to save image URL: ${it.message}")
        }
}