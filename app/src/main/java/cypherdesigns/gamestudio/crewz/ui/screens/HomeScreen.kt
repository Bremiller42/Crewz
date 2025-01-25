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
    onAccountSettings: () -> Unit,
    onCrewSettings: () -> Unit,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit,
    onBack: () -> Unit
) {
    val crewId by userViewModel.currentCrewId.collectAsState()
    val crewName by crewViewModel.currentCrewName.collectAsState()
    val userName = userViewModel.cachedUserName
    val firstName = userViewModel.cachedFirstName

    // Observe crew members so the list is up to date
    LaunchedEffect(crewId) {
        crewId?.let {
            crewViewModel.fetchCrewName(it)
            crewViewModel.observeCrewMembers(it) }
    }
    val displayName = crewName ?: "Loading..."

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = displayName,
                crewViewModel = crewViewModel,
                onAccountSettings = onAccountSettings,
                onCrewSettings = onCrewSettings,
                onLogout = onLogout,
                onMenuClick = onMenuClick,
                onBack = onBack
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
            Text(text = "Welcome $firstName", style = androidx.compose.material3.MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Navigate using the bottom navigation bar! Git check")
        }
    }
}
