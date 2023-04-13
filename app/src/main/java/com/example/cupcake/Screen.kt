package com.example.cupcake

import androidx.annotation.StringRes

// TODO: Step1 - Define and Create the Screen
enum class Screen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Flavor(title = R.string.choose_flavor),
    Pickup(title = R.string.choose_pickup_date),
    Summary(title = R.string.order_summary)
}


