package cypherdesigns.gamestudio.crewz.ui

import GalleryScreen
import MapScreen
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.storage.FirebaseStorage
import cypherdesigns.gamestudio.crewz.ui.screens.HomeScreen
import cypherdesigns.gamestudio.crewz.ui.login.LoginScreen
import cypherdesigns.gamestudio.crewz.ui.login.RegisterScreen
import cypherdesigns.gamestudio.crewz.ui.screens.ChatroomListScreen
import cypherdesigns.gamestudio.crewz.ui.screens.ChatroomScreen
import cypherdesigns.gamestudio.crewz.ui.screens.CrewSelectionScreen
import cypherdesigns.gamestudio.crewz.ui.screens.CrewSidebar
import cypherdesigns.gamestudio.crewz.ui.screens.ImageDetailScreen
import cypherdesigns.gamestudio.crewz.ui.screens.SettingsScreen
import cypherdesigns.gamestudio.crewz.ui.screens.UploadImageScreen
import cypherdesigns.gamestudio.crewz.viewmodel.ChatViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.GalleryViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val bottomBarRoutes = listOf("home", "map", "chat", "gallery")
    val chatViewModel: ChatViewModel = viewModel()
    val galleryViewModel: GalleryViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val crewId by userViewModel.currentCrewId.collectAsState()

    val storageReference = FirebaseStorage.getInstance().reference

    // Drawer state and coroutine scope
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        crewId?.let { userViewModel.observeCrewMembers(it) }
    }

    // Collect the user statuses
    val crewMembers by userViewModel.crewMembers.collectAsState()



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 85.dp)
                    .padding(bottom = 50.dp) // Reserve space for the BottomNavigationBar
                    .offset(x = -20.dp)
            ) {
                CrewSidebar(
                    crewMembers = crewMembers,
                    onMemberClick = { member ->
                        println("Clicked on member: ${member.name}")
                    }
                )
            }
        }
    ) {
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
            Box(modifier = Modifier.fillMaxSize()) {
                // Main Navigation Content
                NavHost(
                    navController = navController,
                    startDestination = "login",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("login") {
                        val context = LocalContext.current
                        LoginScreen(
                            viewModel = userViewModel,
                            onLoginSuccess = {
                                val currentUserId = userViewModel.currentUserId
                                if (currentUserId != null) {
                                    userViewModel.fetchCrewId(currentUserId) { crewId ->
                                        if (!crewId.isNullOrEmpty()) {
                                            userViewModel.updateOnlineStatus(crewId, currentUserId, true)
                                            userViewModel.observeCrewMembers(crewId)
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("crewSelection") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: User not logged in",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                            viewModel = UserViewModel(),
                            crewId = crewId!!,
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
                    }
                    composable("map") {
                        MapScreen(
                            userViewModel = userViewModel,
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            })
                    }
                    composable("gallery") {
                        GalleryScreen(
                            viewModel = galleryViewModel,
                            userViewModel = userViewModel,
                            onNavigateToUploadScreen = { navController.navigate("upload") },
                            onImageClick = { imageData ->
                                navController.navigate(
                                    "imageDetail?imageUrl=${Uri.encode(imageData.url)}&uploadedBy=${
                                        Uri.encode(
                                            imageData.uploadedBy
                                        )
                                    }"
                                )
                            },
                            crewId = crewId!!,
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            })
                    }
                    composable(
                        route = "imageDetail?imageUrl={imageUrl}&uploadedBy={uploadedBy}",
                        arguments = listOf(
                            navArgument("imageUrl") { type = NavType.StringType },
                            navArgument("uploadedBy") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val imageUrl = backStackEntry.arguments?.getString("imageUrl")
                        val uploadedBy =
                            backStackEntry.arguments?.getString("uploadedBy")
                                ?.let { Uri.decode(it) }
                        ImageDetailScreen(
                            imageUrl = imageUrl,
                            uploadedBy = uploadedBy,
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            })
                    }

                    composable("upload") {
                        UploadImageScreen(
                            storageReference = storageReference,
                            galleryViewModel = galleryViewModel,
                            userViewModel = userViewModel,
                            onNavigateToGalleryScreen = { navController.navigate("gallery") }
                        )
                    }
                    composable("chat") {
                        ChatroomListScreen(
                            viewModel = chatViewModel,
                            userViewModel = userViewModel,
                            onChatroomSelected = { chatroomId ->
                                navController.navigate("chatroom/$chatroomId")
                            },
                            crewId = crewId!!,
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            })
                    }
                    composable("chatroom/{chatroomId}") { backStackEntry ->
                        val chatroomId =
                            backStackEntry.arguments?.getString("chatroomId")
                                ?: return@composable
                        ChatroomScreen(
                            viewModel = chatViewModel,
                            userViewModel = userViewModel,
                            crewId = crewId!!,
                            chatroomId = chatroomId,
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            })
                    }
                    composable("settings") {
                        if (crewId != null && userViewModel.currentUserId != null) {
                            SettingsScreen(
                                viewModel = userViewModel,
                                userId = userViewModel.currentUserId,
                                crewId = crewId!!,
                                onBack = { navController.popBackStack() },
                                onMenuClick = {
                                    coroutineScope.launch { drawerState.open() }
                                })
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Unable to load settings. Missing required information.")
                            }
                        }
                    }
                    composable("checkCrew") {
                        LaunchedEffect(crewId) {
                            if (crewId.isNullOrEmpty()) {
                                navController.navigate("crewSelection") {
                                    popUpTo("checkCrew") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("checkCrew") { inclusive = true }
                                }
                            }
                        }
                    }
                    composable("crewSelection") {
                        CrewSelectionScreen(
                            userViewModel = userViewModel,
                            onCrewSelected = { crewId ->
                                userViewModel.updateCrewId(crewId)
                                navController.navigate("home") {
                                    popUpTo("crewSelection") { inclusive = true }
                                }
                            },
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            })
                    }
                }
            }
        }
    }
}