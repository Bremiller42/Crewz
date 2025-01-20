package cypherdesigns.gamestudio.crewz.ui.map

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

data class CrewMemberLocation(
    val userName: String,
    val location: LatLng,
    val vectorResId: Int
)
