package com.shifthackz.aisdv1.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.android.core.mvi.MviComponent
import com.shifthackz.aisdv1.presentation.model.NavItem
import com.shifthackz.aisdv1.presentation.navigation.NavigationRoute.HomeNavigation
import com.shifthackz.aisdv1.presentation.widget.connectivity.ConnectivityComposable
import com.shifthackz.aisdv1.presentation.widget.item.NavigationItemIcon
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeNavigationScreen(
    navItems: List<NavItem> = emptyList(),
) {
    require(navItems.isNotEmpty()) { "navItems collection must not be empty." }

    val viewModel = koinViewModel<HomeNavigationViewModel>()
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry.value?.destination

    LaunchedEffect(currentDestination) {
        if (currentDestination?.hasRoute(HomeNavigation.TxtToImg::class) == true) {
            viewModel.processIntent(HomeNavigationIntent.Update(HomeNavigation.TxtToImg))
        }
    }

    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            navController.navigate(effect.navRoute) {
                navController.graph.startDestinationRoute?.let { route ->
                    popUpTo(route) {
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }
        },
    ) { _, processIntent ->
        Scaffold(
            modifier = Modifier.imePadding(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    navItems.forEach { item ->
                        val selected =
                            currentDestination?.route?.contains("${item.navRoute}") == true
                        NavigationBarItem(
                            selected = selected,
                            label = {
                                Text(
                                    text = item.name.asString(),
                                    color = LocalContentColor.current,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors().copy(
                                selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                            ),
                            icon = { NavigationItemIcon(item.icon) },
                            onClick = { processIntent(HomeNavigationIntent.Route(item.navRoute)) },
                        )
                    }
                }
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        ConnectivityComposable()
                    }
                    NavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = navItems.first().navRoute,
                    ) {
                        navItems.forEach { item ->
                            when (item.navRoute as HomeNavigation) {
                                HomeNavigation.Gallery -> composable<HomeNavigation.Gallery> {
                                    item.content?.invoke()
                                }

                                HomeNavigation.TxtToImg -> composable<HomeNavigation.TxtToImg> {
                                    item.content?.invoke()
                                }

                                HomeNavigation.ImgToImg -> composable<HomeNavigation.ImgToImg> {
                                    item.content?.invoke()
                                }

                                HomeNavigation.Settings -> composable<HomeNavigation.Settings> {
                                    item.content?.invoke()
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
