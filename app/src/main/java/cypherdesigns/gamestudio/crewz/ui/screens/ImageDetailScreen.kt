package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import cypherdesigns.gamestudio.crewz.ui.screens.AppTopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel

@Composable
fun ImageDetailScreen(
    imageUrl: String?,
    crewViewModel: CrewViewModel,
    uploadedBy: String?,
    onAccountSettings: () -> Unit,
    onCrewSettings: () -> Unit,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Image",
                crewViewModel = crewViewModel,
                onAccountSettings = onAccountSettings,
                onCrewSettings = onCrewSettings,
                onLogout = onLogout,
                onMenuClick = onMenuClick,
                onBack = onBack
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Uploaded by: ${uploadedBy ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Image uploaded by $uploadedBy",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
