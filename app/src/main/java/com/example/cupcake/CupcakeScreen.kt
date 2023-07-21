/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cupcake

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cupcake.data.DataSource.flavors
import com.example.cupcake.data.DataSource.quantityOptions
import com.example.cupcake.ui.OrderSummaryScreen
import com.example.cupcake.ui.OrderViewModel
import com.example.cupcake.ui.SelectOptionScreen
import com.example.cupcake.ui.StartOrderScreen

// https://developer.android.com/jetpack/compose/navigation
// https://developer.android.com/codelabs/basic-android-kotlin-compose-navigation

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */

@Composable
fun CupcakeAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun CupcakeApp(modifier: Modifier = Modifier, viewModel: OrderViewModel = viewModel()) {
    // TODO: Step2 - Create NavController
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val titleState = mainViewModel.titleState.collectAsState()

    Scaffold(
        topBar = {
            CupcakeAppBar(
                title = titleState.value,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = {
                    // TODO: implement back navigation
                    navController.navigateUp()
                }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        // TODO: Step3 - add NavHost
        NavHost(
            navController = navController,
            startDestination = Screen.Start.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = Screen.Start.route) {
                mainViewModel.updateTitle(Screen.Start.name)
                StartOrderScreen(
                    quantityOptions = quantityOptions,
                    onNextButtonClicked = {
                        viewModel.setQuantity(it)
                        navController.navigate(Screen.Flavor.route)
                    }
                )
            }
            composable(route = Screen.Flavor.route) {
                mainViewModel.updateTitle(Screen.Flavor.name)

                val context = LocalContext.current

                SelectOptionScreen(

                    subtotal = uiState.price,
                    onNextButtonClicked = {

                        navController.navigate(Screen.Pickup.route + "/${uiState.flavor}")
                    },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    options = flavors.map { id -> context.resources.getString(id) },
                    onSelectionChanged = { viewModel.setFlavor(it) }
                )

            }
            composable(
                route = Screen.Pickup.route + "/{flavorID}",
                arguments = listOf(navArgument("flavorID") { type = NavType.StringType })
            ) { backStackEntry ->
                val flavorID = backStackEntry.arguments?.getString("flavorID")
                Log.d("CupcakeApp", "flavorID: $flavorID")

                mainViewModel.updateTitle(flavorID ?: Screen.Pickup.name)

                SelectOptionScreen(

                    subtotal = uiState.price,
                    onNextButtonClicked = {

                        navController.navigate(Screen.Summary.route) {
                            // popUpTo(Screen.Pickup.route) { inclusive = true }
                        }
                    },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    options = uiState.pickupOptions,
                    onSelectionChanged = { viewModel.setDate(it) }
                )

            }
            composable(route = Screen.Summary.route) {
                val context = LocalContext.current
                mainViewModel.updateTitle( Screen.Summary.name)

                OrderSummaryScreen(

                    orderUiState = uiState,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    onSendButtonClicked = { subject: String, summary: String ->
                        shareOrder(context, subject = subject, summary = summary)
                    }
                )
            }
        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(Screen.Start.route, inclusive = false)
}

private fun shareOrder(context: Context, subject: String, summary: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }

    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_cupcake_order)
        )
    )
}
