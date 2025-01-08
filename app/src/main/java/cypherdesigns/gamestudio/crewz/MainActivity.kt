package cypherdesigns.gamestudio.crewz

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import cypherdesigns.gamestudio.crewz.ui.AppNavigation
import cypherdesigns.gamestudio.crewz.ui.theme.CrewzTheme

class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrewzTheme(dynamicColor = false, darkTheme = true) {
                AppNavigation()
            }
        }
    }
}
