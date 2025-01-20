package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    crewViewModel: CrewViewModel,
    onSettingsClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val crewId by userViewModel.currentCrewId.collectAsState()

    // Observe crew members so the list is up to date
    LaunchedEffect(crewId) {
        crewId?.let { crewViewModel.observeCrewMembers(it) }
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Our Crew",
                onSettingsClick = onSettingsClick,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Welcome to Crewz", style = androidx.compose.material3.MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Navigate using the bottom navigation bar!")
        }
    }
}
