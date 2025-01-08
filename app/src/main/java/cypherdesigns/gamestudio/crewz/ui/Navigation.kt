package cypherdesigns.gamestudio.crewz.ui

import GalleryScreen
import MapScreen
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import cypherdesigns.gamestudio.crewz.data.repository.UserRepository
import cypherdesigns.gamestudio.crewz.ui.screens.HomeScreen
import cypherdesigns.gamestudio.crewz.ui.login.LoginScreen
import cypherdesigns.gamestudio.crewz.ui.login.RegisterScreen
import cypherdesigns.gamestudio.crewz.ui.screens.ChatroomListScreen
import cypherdesigns.gamestudio.crewz.ui.screens.ChatroomScreen
import cypherdesigns.gamestudio.crewz.ui.screens.ImageDetailScreen
import cypherdesigns.gamestudio.crewz.ui.screens.SettingsScreen
import cypherdesigns.gamestudio.crewz.ui.screens.UploadImageScreen
import cypherdesigns.gamestudio.crewz.viewmodel.ChatViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.GalleryViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.LocationViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import kotlinx.coroutines.flow.map

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val bottomBarRoutes = listOf("home", "map", "chat", "gallery") // Routes that should show the BottomBar
    val chatViewModel: ChatViewModel = viewModel()
    val galleryViewModel: GalleryViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    val storageReference = FirebaseStorage.getInstance().reference

    Scaffold(
        bottomBar = {
            val currentRoute by navController.currentBackStackEntryFlow
                .map { it.destination.route }
                .collectAsState(initial = null)
            if (currentRoute in bottomBarRoutes) {
                BottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = userViewModel,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    onSettingsClick = {navController.navigate("settings")})
            }
            composable("map") {
                MapScreen(
                    onSettingsClick = {navController.navigate("settings")}) // Placeholder
            }
            composable("gallery") {
                GalleryScreen(
                    viewModel = galleryViewModel,
                    onNavigateToUploadScreen = { navController.navigate("upload") },
                    onImageClick = { imageData ->
                        navController.navigate("imageDetail?imageUrl=${Uri.encode(imageData.url)}&uploadedBy=${Uri.encode(imageData.uploadedBy)}")
                    },
                    onSettingsClick = {navController.navigate("settings")}
                )
            }
            composable(
                route = "imageDetail?imageUrl={imageUrl}&uploadedBy={uploadedBy}",
                arguments = listOf(
                    navArgument("imageUrl") { type = NavType.StringType },
                    navArgument("uploadedBy") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val imageUrl = backStackEntry.arguments?.getString("imageUrl")
                val uploadedBy = backStackEntry.arguments?.getString("uploadedBy")?.let { Uri.decode(it) }
                ImageDetailScreen(imageUrl = imageUrl, uploadedBy = uploadedBy)
            }

            composable("upload") {
                UploadImageScreen(storageReference = storageReference, galleryViewModel = galleryViewModel, userViewModel = userViewModel, onNavigateToGalleryScreen = { navController.navigate("gallery") })
            }
            composable("chat") {
                ChatroomListScreen(
                    viewModel = chatViewModel,
                    onChatroomSelected = { chatroomId ->
                        navController.navigate("chatroom/$chatroomId")
                    },
                    onSettingsClick = {navController.navigate("settings")}
                )
            }
            composable("chatroom/{chatroomId}") { backStackEntry ->
                val chatroomId = backStackEntry.arguments?.getString("chatroomId") ?: return@composable
                ChatroomScreen(viewModel = chatViewModel, chatroomId = chatroomId)
            }
            composable("settings") {
                userViewModel.currentUserId?.let { it1 ->
                    SettingsScreen(
                        viewModel = userViewModel,
                        userId = it1,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
