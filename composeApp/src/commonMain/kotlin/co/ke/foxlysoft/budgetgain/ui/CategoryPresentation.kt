package co.ke.foxlysoft.budgetgain.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import co.ke.foxlysoft.budgetgain.database.CategoryEntity
import co.ke.foxlysoft.budgetgain.database.SubCategoryEntity
import co.ke.foxlysoft.budgetgain.database.resolvedColorArgb
import co.ke.foxlysoft.budgetgain.database.resolvedIconKey

fun CategoryEntity.displayColor(isDarkMode: Boolean): Color =
    Color(if (isDarkMode) darkColorArgb else lightColorArgb)

fun SubCategoryEntity.displayColor(parent: CategoryEntity, isDarkMode: Boolean): Color =
    Color(resolvedColorArgb(parent, isDarkMode))

fun CategoryEntity.displayIcon(): ImageVector = MaterialCategoryIconRegistry.resolve(iconKey)

fun SubCategoryEntity.displayIcon(parent: CategoryEntity): ImageVector =
    MaterialCategoryIconRegistry.resolve(resolvedIconKey(parent))

object MaterialCategoryIconRegistry {
    fun resolve(key: String): ImageVector = when (key) {
        "celebration" -> Icons.Default.Celebration
        "local_movies" -> Icons.Default.LocalMovies
        "family" -> Icons.Default.FamilyRestroom
        "restaurant" -> Icons.Default.Restaurant
        "shopping_cart" -> Icons.Default.ShoppingCart
        "eco" -> Icons.Default.Eco
        "home" -> Icons.Default.Home
        "hotel" -> Icons.Default.Hotel
        "shopping_bag" -> Icons.Default.ShoppingBag
        "local_hospital" -> Icons.Default.LocalHospital
        "medication" -> Icons.Default.Medication
        "medical_services" -> Icons.Default.MedicalServices
        "lightbulb" -> Icons.Default.Lightbulb
        "delete" -> Icons.Default.Delete
        "build" -> Icons.Default.Build
        "water_drop" -> Icons.Default.WaterDrop
        "wifi" -> Icons.Default.Wifi
        "smartphone" -> Icons.Default.Smartphone
        "checkroom" -> Icons.Default.Checkroom
        "credit_card" -> Icons.Default.CreditCard
        "music_note" -> Icons.Default.MusicNote
        "tv" -> Icons.Default.Tv
        "directions_car" -> Icons.Default.DirectionsCar
        "two_wheeler" -> Icons.Default.TwoWheeler
        "directions_bus" -> Icons.Default.DirectionsBus
        "local_taxi" -> Icons.Default.LocalTaxi
        "local_gas_station" -> Icons.Default.LocalGasStation
        "rickshaw" -> Icons.Default.TwoWheeler
        "propane_tank" -> Icons.Default.LocalGasStation
        else -> Icons.Default.Category
    }
}
