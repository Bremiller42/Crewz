package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.ui.memberlist.CrewMember

@Composable
fun CrewSidebar(
    crewMembers: List<CrewMember>,
    onMemberClick: (CrewMember) -> Unit
) {
    val onlineMembers = crewMembers.filter { it.online }
    val offlineMembers = crewMembers.filterNot { it.online }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 2.dp, // Border thickness
                color = MaterialTheme.colorScheme.primary // Border color
            )            .fillMaxHeight()
            .width(200.dp)
            .padding(16.dp)
    ) {
        Text("Online", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        onlineMembers.forEach { member ->
            Text(
                text = member.name,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onMemberClick(member) }
                    .padding(vertical = 4.dp)

            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Offline", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(8.dp))
        offlineMembers.forEach { member ->
            Text(
                text = member.name,
                color = MaterialTheme.colorScheme.secondary,

                modifier = Modifier
                    .clickable { onMemberClick(member) }
                    .padding(vertical = 4.dp)
            )
        }
    }
}
