package co.ke.foxlysoft.budgetgain

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform