package com.example.cupcake

import androidx.annotation.StringRes

// TODO: Step1 - Define and Create the Screen
enum class Screen(val route: String ) {
    Start(route = "Start"),
    Flavor(route = "Flavor/{flavorID}"),
    Pickup(route = "Pickup"),
    Summary(route = "Summary")
}


