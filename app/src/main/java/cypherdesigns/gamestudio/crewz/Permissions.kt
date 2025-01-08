package cypherdesigns.gamestudio.crewz

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.Text
import androidx.core.app.ActivityCompat

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun RequestMediaPermission() {
    val context = LocalContext.current
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        requiredPermission
                    )
                ) {
                    // Show rationale for permission
                    Toast.makeText(
                        context,
                        "Permission is needed to access media files.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Guide the user to app settings
                    Toast.makeText(
                        context,
                        "Permission denied. Please enable it in app settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    Button(onClick = {
        permissionLauncher.launch(requiredPermission)
    }) {
        Text("Request Permission")
    }
}


@Composable
fun RequestLocationPermission(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Location Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Location Permission Denied", Toast.LENGTH_SHORT).show()
            }
            onPermissionResult(isGranted)
        }
    )

    Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
        Text("Request Location Permission")
    }
}
