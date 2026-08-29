package co.ke.foxlysoft.budgetgain.database

data class CommonSubCategoryDefinition(
    val key: String,
    val name: String,
    val iconKey: String? = null,
    val lightColorArgb: Long? = null,
    val darkColorArgb: Long? = null,
)

data class CommonCategoryDefinition(
    val key: String,
    val name: String,
    val lightColorArgb: Long,
    val darkColorArgb: Long,
    val iconKey: String,
    val subCategories: List<CommonSubCategoryDefinition> = emptyList(),
)

object CommonCategoryCatalog {
    val categories = listOf(
        category("entertainment", "Entertainment", 0xFFF57C00, 0xFFFFB74D, "celebration",
            sub("movies", "Movies", "local_movies")),
        category("family", "Family", 0xFF7B1FA2, 0xFFBA68C8, "family"),
        category("food", "Food", 0xFF388E3C, 0xFF81C784, "restaurant",
            sub("bulk", "Bulk", "shopping_cart"),
            sub("groceries", "Groceries", "eco"),
            sub("household", "Household", "home"),
            sub("restaurants", "Restaurants", "hotel"),
            sub("takeout", "Takeout", "shopping_bag")),
        category("healthcare", "Healthcare", 0xFF1976D2, 0xFF64B5F6, "local_hospital",
            sub("medicine", "Medicine", "medication"),
            sub("consultation", "Consultation", "medical_services")),
        category("household", "Household", 0xFFF06292, 0xFFF48FB1, "home",
            sub("cleaning", "Cleaning"),
            sub("electricity", "Electricity", "lightbulb"),
            sub("garbage", "Garbage", "delete"),
            sub("gas", "Gas", "propane_tank"),
            sub("rent", "Rent", "home"),
            sub("service", "Service", "build"),
            sub("water", "Water", "water_drop"),
            sub("wifi", "Wifi", "wifi")),
        category("mobile", "Mobile", 0xFF0097A7, 0xFF4DD0E1, "smartphone",
            sub("airtime", "Airtime"), sub("bundles", "Bundles")),
        category("shopping", "Shopping", 0xFF5D4037, 0xFFA1887F, "shopping_bag",
            sub("clothing", "Clothing", "checkroom"),
            sub("electronics", "Electronics"), sub("general", "General")),
        category("subscriptions", "Subscriptions", 0xFF303F9F, 0xFF7986CB, "credit_card",
            sub("music", "Music", "music_note"), sub("streaming", "Streaming", "tv")),
        category("transport", "Transport", 0xFFFFA000, 0xFFFFD54F, "directions_car",
            sub("bodaboda", "Bodaboda", "two_wheeler"),
            sub("tuktuk", "Tuktuk", "rickshaw"),
            sub("matatu", "Matatu", "directions_bus"),
            sub("taxi", "Taxi", "local_taxi"),
            sub("fuel", "Fuel", "local_gas_station")),
    )

    fun find(key: String): CommonCategoryDefinition? = categories.firstOrNull { it.key == key }

    fun normalizedKey(name: String): String {
        return name
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    private fun category(
        key: String,
        name: String,
        lightColorArgb: Long,
        darkColorArgb: Long,
        iconKey: String,
        vararg subCategories: CommonSubCategoryDefinition,
    ) = CommonCategoryDefinition(key, name, lightColorArgb, darkColorArgb, iconKey, subCategories.toList())

    private fun sub(key: String, name: String, iconKey: String? = null) =
        CommonSubCategoryDefinition(key, name, iconKey)
}
