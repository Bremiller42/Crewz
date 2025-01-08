package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onSettingsClick: () -> Unit) {
    Scaffold(
        topBar = { AppTopAppBar(title = "Our Crew", onSettingsClick = onSettingsClick ) },

        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Welcome to Crewz", style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Navigate using the bottom navigation bar!")
        }
    }
}
