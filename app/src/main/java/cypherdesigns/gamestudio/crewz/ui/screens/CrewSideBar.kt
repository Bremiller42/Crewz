package cypherdesigns.gamestudio.crewz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cypherdesigns.gamestudio.crewz.ui.memberlist.CrewMember
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun CrewSidebar(
    userViewModel: UserViewModel,
    onMemberClick: (CrewMember) -> Unit
) {

    val crewMembers by userViewModel.crewMembers.collectAsState()

    val onlineMembers = crewMembers.filter { it.online }
    val offlineMembers = crewMembers.filterNot { it.online }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sidebarWidth = screenWidth / 2 // Half of the screen width

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 2.dp, // Border thickness
                color = MaterialTheme.colorScheme.primary // Border color
            )
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()) // Enable scrolling
            .width(sidebarWidth)
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Members",
            style = MaterialTheme.typography.headlineMedium,
//            color = MaterialTheme.colorScheme.primary,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            "Online",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        onlineMembers.forEach { member ->
            Text(
                text = member.name,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onMemberClick(member) }

            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Offline",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        offlineMembers.forEach { member ->
            Text(
                text = member.name,
                color = MaterialTheme.colorScheme.onBackground,

                modifier = Modifier
                    .clickable { onMemberClick(member) }
            )
        }
    }
}
