import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.data.ImageData
import cypherdesigns.gamestudio.crewz.ui.screens.AppTopAppBar
import cypherdesigns.gamestudio.crewz.viewmodel.GalleryViewModel

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onNavigateToUploadScreen: () -> Unit,
    onImageClick: (ImageData) -> Unit, // Pass the entire ImageData object on click
    onSettingsClick: () -> Unit
) {
    val imageDataList = viewModel.imageUrls.collectAsState().value // List of ImageData

    LaunchedEffect(Unit) {
        viewModel.fetchImageUrls()
    }
    Scaffold(
        topBar = { AppTopAppBar(title = "Crew Gallery", onSettingsClick = onSettingsClick ) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToUploadScreen,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "Upload Image",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(imageDataList) { imageData ->
                Column(modifier = Modifier
                    .clickable { onImageClick(imageData) }
                    .background(MaterialTheme.colorScheme.background)) {
                    Text(
                        text = "Uploaded by: ${imageData.uploadedBy}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    AsyncImage(
                        model = imageData.url,
                        contentDescription = "Gallery Image",
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(4.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
}
