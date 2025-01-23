package cypherdesigns.gamestudio.crewz.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.ui.screens.AppTopAppBar
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import cypherdesigns.gamestudio.crewz.ui.map.MemberLocation
import kotlinx.coroutines.delay
import vectorToBitmapDescriptor

@Composable
fun MapScreen(
    userViewModel: UserViewModel,
    crewViewModel: CrewViewModel,
    onAccountSettings: () -> Unit,
    onCrewSettings: () -> Unit,
    onLogout: () -> Unit,
    onMenuClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hasLocationPermission = remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission.value = granted
        if (!granted) {
            Toast.makeText(
                context,
                "Location permission denied. Unable to show current location.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val crewId by userViewModel.currentCrewId.collectAsState()

    // Request permission if not already granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission.value) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            if (crewId != null) {
                crewViewModel.observeCrewMembers(crewId!!)
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = "Map",
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
                onClick = { /* Your navigation logic */ },
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_navigation),
                    contentDescription = "Navigate",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.background
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        if (hasLocationPermission.value) {
            MapContent(
                userViewModel = userViewModel,
                crewViewModel = crewViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Location permission is required to display the map.")
            }
        }
    }
}
@Composable
fun MapContent(
    userViewModel: UserViewModel,
    crewViewModel: CrewViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val crewId by userViewModel.currentCrewId.collectAsState()
    val userId by userViewModel.currentUserId.collectAsState()

    val userLocationEnabled by crewViewModel.isLocationSharingEnabled.collectAsState()
    val selectedHue by crewViewModel.markerColorName.collectAsState()

    // 1) Collect the list of location-sharing members from the ViewModel
    val crewLocations by crewViewModel.crewLocations.collectAsState()

    // We'll store just the user's location in local state
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var previousLocation by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState()
    var isFollowingUser by remember { mutableStateOf(true) }

    // 2) Start observing location data from the CrewViewModel
    LaunchedEffect(crewId) {
        crewId?.let {
            crewViewModel.observeCrewLocations(it)
        }
    }

    // 3) Start location updates for the current user
    DisposableEffect(Unit) {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            2500L
        ).build()
        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                val location = result.lastLocation ?: return
                val newLocation = LatLng(location.latitude, location.longitude)
                val distanceMoved = previousLocation?.let {
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        it.latitude, it.longitude,
                        newLocation.latitude, newLocation.longitude,
                        results
                    )
                    results[0]
                } ?: Float.MAX_VALUE

                if (distanceMoved >= 10) {
                    previousLocation = newLocation
                    currentLocation = newLocation
                    isFollowingUser = true

                    if (userLocationEnabled && userId != null && crewId != null) {
                        val updates = mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                            "markerColor" to selectedHue,
                            "locationSharingEnabled" to userLocationEnabled
                        )
                        crewViewModel.updateCrewUserInfo(
                            crewId = crewId!!,
                            userId = userId!!, // now userId is the actual UID string
                            updates = updates
                        )
                    }

                    if (isFollowingUser) {
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(newLocation, 15f)
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // 4) Display the map using the user’s currentLocation + the crewLocations from the ViewModel
    if (currentLocation != null) {
        MapViewContent(
            currentLocation = currentLocation!!,
            crewLocations = crewLocations,  // Now from the VM
            cameraPositionState = cameraPositionState,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading map...")
        }
    }
}


@Composable
fun MapViewContent(
    currentLocation: LatLng,
    crewLocations: List<MemberLocation>,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier
) {
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId("b1a5c5082bb767d")
        },
        properties = MapProperties(
            isMyLocationEnabled = true,
            isTrafficEnabled = true,
            mapType = MapType.NORMAL,
            minZoomPreference = 3f,
            maxZoomPreference = 20f
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = true,
            compassEnabled = true
        )
    ) {
        crewLocations.forEach { member ->
            val markerIcon = vectorToBitmapDescriptor(
                LocalContext.current,
                colorNameToVector(member.markerColor)
            )
            Marker(
                state = MarkerState(position = LatLng(member.latitude, member.longitude)),
                title = member.userName,
                icon = markerIcon
            )
        }
    }
}

/**
 * Convert a color name (e.g. "red") to the appropriate vector resource for the marker.
 */
fun colorNameToVector(colorName: String): Int {
    return when (colorName) {
        "red" -> R.drawable.ic_crew_marker_red
        "orange" -> R.drawable.ic_crew_marker_orange
        "yellow" -> R.drawable.ic_crew_marker_yellow
        "green" -> R.drawable.ic_crew_marker_green
        "blue" -> R.drawable.ic_crew_marker_blue
        "cyan" -> R.drawable.ic_crew_marker_cyan
        "magenta" -> R.drawable.ic_crew_marker_magenta
        "purple" -> R.drawable.ic_crew_marker_purple
        "black" -> R.drawable.ic_crew_marker_black
        "gray" -> R.drawable.ic_crew_marker_gray
        "white" -> R.drawable.ic_crew_marker_white
        else -> R.drawable.ic_crew_marker_red
    }
}
