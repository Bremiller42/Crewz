package cypherdesigns.gamestudio.crewz.data

data class ImageData(
    val url: String,
    val uploadedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)
