package cypherdesigns.gamestudio.crewz.ui

import GalleryScreen
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.storage.FirebaseStorage
import cypherdesigns.gamestudio.crewz.data.utilities.AppLifecycleObserver
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
import cypherdesigns.gamestudio.crewz.viewmodel.CrewViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.GalleryViewModel
import cypherdesigns.gamestudio.crewz.viewmodel.UserViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Common bottom bar routes
    val bottomBarRoutes = listOf("home", "map", "chat", "gallery")

    // Instantiate all needed ViewModels
    val chatViewModel: ChatViewModel = viewModel()
    val galleryViewModel: GalleryViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val crewViewModel: CrewViewModel = viewModel()

    // Observe the user's crewId (stored in /users/{userId}/crewId)
    val crewId by userViewModel.currentCrewId.collectAsState()
    val userId = userViewModel.currentUserId

    val storageReference = FirebaseStorage.getInstance().reference

    // Drawer state
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Once we know the crewId, observe crew-level data
    LaunchedEffect(crewId) {
        crewId?.let { cId ->
            if (userId != null) {
                // Observe the crew's members and user connection status
                crewViewModel.observeCrewMembers(cId)
                crewViewModel.observeConnectionStatus(cId, userId)
            }

            // We can also attach a lifecycle observer that sets user online/offline
            if (userId != null) {
                val lifecycleObserver = AppLifecycleObserver(cId, userId) { isOnline ->
                    println("User $userId is now ${if (isOnline) "online" else "offline"}")
                }
                ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
            }
        }
    }

    // Collect the crew members from CrewViewModel
    val crewMembers by crewViewModel.crewMembers.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxHeight()
                    .padding(top = 85.dp)
                    .padding(bottom = 50.dp)
                    .offset(x = (-20).dp)
            ) {
                // Updated to use CrewViewModel
                CrewSidebar(
                    crewViewModel = crewViewModel,
                    onMemberClick = { member ->
                        println("Clicked on member: ${member.userName}")
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
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = "login",
                    modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = userViewModel,
                            onLoginSuccess = {
                                val currentUserId = userViewModel.currentUserId
                                if (currentUserId != null) {
                                    // Once user logs in, we fetch the crewId from /users
                                    userViewModel.fetchCrewId(currentUserId) { fetchedCrewId ->
                                        if (!fetchedCrewId.isNullOrEmpty()) {
                                            // Mark them online in the crew
                                            crewViewModel.updateOnlineStatus(
                                                fetchedCrewId,
                                                currentUserId,
                                                true
                                            )
                                            crewViewModel.observeCrewMembers(fetchedCrewId)
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
                                        it,
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
                            viewModel = UserViewModel(),
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
                        // We pass both userViewModel & crewViewModel
                        if (crewId != null) {
                            HomeScreen(
                                userViewModel = userViewModel,
                                crewViewModel = crewViewModel,
                                onSettingsClick = { navController.navigate("settings") },
                                onMenuClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                            )
                        } else {
                            // Edge case: crewId is null
                            Box(
                                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text("No crew selected. Please pick a crew.")
                            }
                        }
                    }

                    composable("map") {
                        MapScreen(
                            userViewModel = userViewModel,
                            crewViewModel = crewViewModel,
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
                    }

                    composable("gallery") {
                        if (crewId != null) {
                            GalleryScreen(
                                viewModel = galleryViewModel,
                                userViewModel = userViewModel,
                                crewId = crewId!!,
                                onNavigateToUploadScreen = { navController.navigate("upload") },
                                onImageClick = { imageData ->
                                    navController.navigate(
                                        "imageDetail?imageUrl=${Uri.encode(imageData.url)}&uploadedBy=${
                                            Uri.encode(imageData.uploadedBy)
                                        }"
                                    )
                                },
                                onSettingsClick = { navController.navigate("settings") },
                                onMenuClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            )
                        } else {
                            // If crewId is null, user hasn't joined a crew yet
                            Box(
                                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text("Join a crew to view the gallery!")
                            }
                        }
                    }
                    composable(
                        route = "imageDetail?imageUrl={imageUrl}&uploadedBy={uploadedBy}",
                        arguments = listOf(
                            navArgument("imageUrl") { type = NavType.StringType },
                            navArgument("uploadedBy") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val imageUrl =
                            backStackEntry.arguments?.getString("imageUrl")
                        val uploadedBy =
                            backStackEntry.arguments?.getString("uploadedBy")?.let { Uri.decode(it) }
                        ImageDetailScreen(
                            imageUrl = imageUrl,
                            uploadedBy = uploadedBy,
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
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
                        if (crewId != null) {
                            ChatroomListScreen(
                                viewModel = chatViewModel,
                                crewViewModel = crewViewModel,
                                userViewModel = userViewModel,
                                crewId = crewId!!,
                                onChatroomSelected = { chatroomId ->
                                    navController.navigate("chatroom/$chatroomId")
                                },
                                onSettingsClick = { navController.navigate("settings") },
                                onMenuClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            )
                        } else {
                            Box(
                                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text("No crew selected. Please join a crew to access chat.")
                            }
                        }
                    }
                    composable("chatroom/{chatroomId}") { backStackEntry ->
                        val chatroomId =
                            backStackEntry.arguments?.getString("chatroomId")
                                ?: return@composable
                        if (crewId != null) {
                            ChatroomScreen(
                                viewModel = chatViewModel,
                                userViewModel = userViewModel,
                                crewViewModel = crewViewModel,
                                crewId = crewId!!,
                                chatroomId = chatroomId,
                                onSettingsClick = { navController.navigate("settings") },
                                onMenuClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            )
                        } else {
                            Box(
                                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text("No crew selected. Please join a crew to chat.")
                            }
                        }
                    }

                    composable("settings") {
                        if (crewId != null && userId != null) {
                            SettingsScreen(
                                userViewModel = userViewModel,
                                crewViewModel = crewViewModel,
                                userId = userId,
                                crewId = crewId!!,
                                onBack = { navController.popBackStack() },
                                onMenuClick = {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            )
                        } else {
                            Box(
                                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text("Unable to load settings. Missing required info.")
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
                            crewViewModel = crewViewModel,
                            onCrewSelected = { selectedCrewId ->
                                // 1) Update /users/{userId} node
                                userViewModel.updateCrewId(selectedCrewId)
                                userViewModel.updateUserInfoInUserNode(
                                    crewId = selectedCrewId,
                                    userName = userViewModel.cachedUserName ?: "Unknown User",
                                    firstName = userViewModel.cachedFirstName ?: "Unknown First",
                                    lastName  = userViewModel.cachedLastName  ?: "Unknown Last",
                                    email     = userViewModel.cachedEmail     ?: "Unknown Email"
                                )

                                // 2) Update /crews/{crewId}/members/{userId} node
                                crewViewModel.updateCrewUserInfo(
                                    crewId = selectedCrewId,
                                    userId = userViewModel.currentUserId ?: return@CrewSelectionScreen,
                                    updates = mapOf(
                                        "userName" to (userViewModel.cachedUserName ?: "Unknown User"),
                                        "firstName" to (userViewModel.cachedFirstName ?: "Unknown First"),
                                        "lastName"  to (userViewModel.cachedLastName  ?: "Unknown Last"),
                                        "email"     to (userViewModel.cachedEmail     ?: "Unknown Email"),
                                        "markerColor" to "red",
                                        "locationSharingEnabled" to false
                                    )
                                )

                                navController.navigate("home") {
                                    popUpTo("crewSelection") { inclusive = true }
                                }
                            },
                            onSettingsClick = { navController.navigate("settings") },
                            onMenuClick = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
                    }
                }
            }
        }
    }
}
