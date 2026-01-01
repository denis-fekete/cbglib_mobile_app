package cv.demoapps.bangdemo.data

import kotlinx.serialization.Serializable

@Serializable
data class CardDetail(
    val id: Int,
    val name: String,
    val description: String,
    val symbols: List<Int>,
    val imagePath: String
)