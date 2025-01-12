import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.maps.android.compose.*
import cypherdesigns.gamestudio.crewz.R
import cypherdesigns.gamestudio.crewz.ui.map.CrewMemberLocation
import cypherdesigns.gamestudio.crewz.ui.screens.AppTopAppBar
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel

@Composable
fun MapScreen(userViewModel: UserViewModel, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (!isGranted) {
            Toast.makeText(
                context,
                "Location permission denied. Unable to show current location.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = { AppTopAppBar(title = "Crew Map", onSettingsClick = onSettingsClick) },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { /* Placeholder for navigation action */ },
//                containerColor = MaterialTheme.colorScheme.primary
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_navigation),
//                    contentDescription = "Navigate",
//                    tint = MaterialTheme.colorScheme.background
//                )
//            }
//        }
    ) { innerPadding ->
        if (hasLocationPermission) {
            MapContent(userViewModel, modifier = Modifier.padding(innerPadding))
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val database = FirebaseDatabase.getInstance().getReference("userLocations")

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var previousLocation by remember { mutableStateOf<LatLng?>(null) }

    val crewLocations = remember { mutableStateListOf<CrewMemberLocation>() }

    val currentUserId = userViewModel.currentUserId
    val userFirstName = userViewModel.cachedFirstName
    val selectedHue by userViewModel.markerColorName.collectAsState() // Retrieve hue from ViewModel

    println("Selected Hue: $selectedHue")
    var isFollowingUser by remember { mutableStateOf(true) }
    val cameraPositionState = rememberCameraPositionState()

    // **Update current user location in Firebase**
    DisposableEffect(Unit) {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            2500L // Request updates every 2.5 seconds
        ).build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val newLocation = LatLng(location.latitude, location.longitude)
                    val distanceMoved = previousLocation?.let {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            it.latitude, it.longitude,
                            newLocation.latitude, newLocation.longitude,
                            results
                        )
                        results[0] // Distance in meters
                    } ?: Float.MAX_VALUE // If no previous location, always update

                    if (distanceMoved >= 10) {
                        previousLocation = newLocation // Update the previous location
                        currentLocation = newLocation // Update the current location
                        isFollowingUser = true

                        if (currentUserId != null) {
                            // Fetch the last known marker color from Firebase
                            database.child(currentUserId).child("markerColor").get()
                                .addOnSuccessListener { snapshot ->
                                    val lastKnownColor = snapshot.getValue(String::class.java) ?: "red"
                                    userViewModel.setMarkerColor(lastKnownColor) // Update the ViewModel
                                }
                                .addOnFailureListener {
                                    println("Failed to fetch last known color: ${it.message}")
                                }

                            // Update Firebase with the current user location and marker color
                            val selectedColor = userViewModel.markerColorName.value
                            database.child(currentUserId).setValue(
                                mapOf(
                                    "latitude" to location.latitude,
                                    "longitude" to location.longitude,
                                    "name" to userFirstName,
                                    "markerColor" to selectedColor // Dynamic color from ViewModel
                                )
                            )
                        }

                        if (isFollowingUser) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                newLocation,
                                15f
                            )
                        }
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

    // **Listen for crew locations in Firebase**
    DisposableEffect(Unit) {
        val crewListener = database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                crewLocations.clear()
                snapshot.children.forEach { child ->
                    val name = child.child("name").getValue(String::class.java) ?: "Unknown"
                    val lat = child.child("latitude").getValue(Double::class.java)
                    val lng = child.child("longitude").getValue(Double::class.java)
                    val markerColor = child.child("markerColor").getValue(String::class.java) ?: "red"

                    // Map the markerColor string to a drawable resource ID
                    val vectorResId = when (markerColor) {
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
                        else -> R.drawable.ic_crew_marker_red // Default to red if unknown
                    }

                    if (lat != null && lng != null) {
                        crewLocations.add(CrewMemberLocation(name, LatLng(lat, lng), vectorResId))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to fetch crew locations: ${error.message}")
            }
        })


        onDispose {
            database.removeEventListener(crewListener)
        }
    }

    if (currentLocation != null) {
        MapViewContent(
            currentLocation = currentLocation!!,
            crewLocations = crewLocations,
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
    crewLocations: List<CrewMemberLocation>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 20f)
    }

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
            minZoomPreference = 1f, // Minimum zoom level (world view)
            maxZoomPreference = 20f // Maximum zoom level

        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = true,
            compassEnabled = true
        )
    ) {

        crewLocations.forEach { crewMember ->
            // Directly use vectorResId as it's already an Int
            val markerIcon = vectorToBitmapDescriptor(context, crewMember.vectorResId)

            Marker(
                state = MarkerState(position = crewMember.location),
                title = crewMember.name,
                icon = markerIcon
            )

        }

    }
}