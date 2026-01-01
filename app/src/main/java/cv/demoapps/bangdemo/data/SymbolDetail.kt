package cv.demoapps.bangdemo.data

import kotlinx.serialization.Serializable

@Serializable
data class SymbolDetail(
    val id: Int,
    val name: String,
    val description: String,
    val imagePath: String,
)
