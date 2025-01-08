package cypherdesigns.studio.crewz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng

data class User(
    val name: String,
    val car: String,
    val Location: LatLng
)

@Composable
fun RosterList(users:List<User>) {
    LazyColumn{
        items(users) { user ->
            RosterListItem(user = user)
        }
    }
}

@Composable
fun RosterListItem(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = user.name, style = MaterialTheme.typography.titleLarge)
        Text(text = user.car, style = MaterialTheme.typography.bodyMedium)
        
    }
}
