import android.Manifest
import android.content.pm.PackageManager
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
import cypherdesigns.gamestudio.crewz.ui.screens.AppTopAppBar


@Composable
fun MapScreen(onSettingsClick: () -> Unit) {
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
            MapContent(modifier = Modifier.padding(innerPadding))
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
fun MapContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val database = FirebaseDatabase.getInstance().getReference("userLocations")

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val crewLocations = remember { mutableStateListOf<LatLng>() }

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
                    currentLocation = LatLng(location.latitude, location.longitude)
                    // Update the user's location in Firebase
                    database.child("currentUserId") // Replace with actual user ID
                        .setValue(mapOf("latitude" to location.latitude, "longitude" to location.longitude))
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
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                crewLocations.clear()
                snapshot.children.forEach { child ->
                    val lat = child.child("latitude").getValue(Double::class.java)
                    val lng = child.child("longitude").getValue(Double::class.java)
                    if (lat != null && lng != null) {
                        crewLocations.add(LatLng(lat, lng))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch crew locations: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
    crewLocations: List<LatLng>,
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
        // Marker for the user's location
        Marker(
            state = MarkerState(position = currentLocation),
            title = "You",
            snippet = "Your current location"
        )

        // Markers for crew locations
        crewLocations.forEach { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Crew Member",
                snippet = "Location shared by crew"
            )
        }
    }
}
