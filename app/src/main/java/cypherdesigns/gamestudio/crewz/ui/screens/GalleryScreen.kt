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
import androidx.compose.material3.Scaffold
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
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    userViewModel: UserViewModel,
    crewViewModel: CrewViewModel,
    crewId: String,
    onNavigateToUploadScreen: () -> Unit,
    onImageClick: (ImageData) -> Unit,
    onAccountSettings: () -> Unit,
    onCrewSettings: () -> Unit,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit,
    onBack: () -> Unit
) {
    val imageDataList = viewModel.imageUrls.collectAsState().value

    // If we want to observe crew members in the gallery
    // pass crewViewModel here if needed:
    // e.g. crewViewModel.observeCrewMembers(crewId)
    // but if it's not essential, skip it.

    LaunchedEffect(crewId) {
        viewModel.fetchImages(crewId)
        // If you want to watch crew membership changes in gallery:
        // crewViewModel.observeCrewMembers(crewId)
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Crew Gallery",
                crewViewModel = crewViewModel,
                onAccountSettings = onAccountSettings,
                onCrewSettings = onCrewSettings,
                onLogout = onLogout,
                onMenuClick = onMenuClick,
                onBack = onBack
            )
        },
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
                    Column(
                        modifier = Modifier
                            .clickable { onImageClick(imageData) }
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Text(
                            text = "Uploaded by: ${imageData.uploadedBy}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 4.dp)
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
