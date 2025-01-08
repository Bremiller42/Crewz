package cypherdesigns.gamestudio.crewz

sealed class NavigationItem(val route: String, val icon: Int, val label: String) {
    object Home : NavigationItem("home", R.drawable.navbar_home, "Home")
    object Map : NavigationItem("map", R.drawable.navbar_map, "Map")
    object Chat : NavigationItem("chat", R.drawable.navbar_chat, "Chat")
    object Gallery : NavigationItem("gallery", R.drawable.navbar_gallery, "Gallery")
}