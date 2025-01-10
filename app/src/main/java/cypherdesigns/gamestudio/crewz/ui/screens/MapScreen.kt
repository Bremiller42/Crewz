import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Placeholder for navigation action */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_navigation),
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
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

    // Update current user location in Firebase
    DisposableEffect(Unit) {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            5000L // Request updates every 5 seconds
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
                        if (currentUserId != null) {
                            database.child(currentUserId) // Replace with actual user ID
                                .setValue(
                                    mapOf(
                                        "latitude" to location.latitude,
                                        "longitude" to location.longitude,
                                        "name" to userFirstName // Replace with actual user's name
                                    )
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

    // Listen for crew locations in Firebase
    DisposableEffect(Unit) {
        val crewListener = database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                crewLocations.clear()
                snapshot.children.forEach { child ->
                    val name = child.child("name").getValue(String::class.java) ?: "Unknown"
                    val lat = child.child("latitude").getValue(Double::class.java)
                    val lng = child.child("longitude").getValue(Double::class.java)
                    if (lat != null && lng != null) {
                        crewLocations.add(CrewMemberLocation(name, LatLng(lat, lng)))
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
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId("b1a5c5082bb767d")
        },
        properties = MapProperties(
            isMyLocationEnabled = true
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = true
        )
    ) {

        // Markers for crew locations
        crewLocations.forEach { crewMember ->
            Marker(
                state = MarkerState(position = crewMember.location),
                title = crewMember.name,
                snippet = "Location shared by crew"
            )
        }
    }
}
