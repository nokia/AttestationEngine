package com.example.mobileattester.ui.util

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobileattester.R
import com.example.mobileattester.ui.pages.*
import com.example.mobileattester.ui.viewmodel.AttestationViewModelImpl
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceDesktop
import compose.icons.tablericons.Dots
import compose.icons.tablericons.Qrcode
import compose.icons.tablericons.QuestionMark

/**
 * Contains all the screens of the application
 * @param route Screen route identifier
 * @param stringResId ResId for the name of the screen
 */
sealed class Screen(val route: String, @StringRes val stringResId: Int) {
    companion object {
        fun getScreenFromRoute(route: String): Screen? {
            return Screen::class.sealedSubclasses.map { it.objectInstance as Screen }
                .firstOrNull { it.route == route }
        }
    }

    object Home : Screen("home", R.string.nav_home)
    object Elements : Screen("elements", R.string.nav_elements)
    object Scanner : Screen("scanner", R.string.nav_scanner)
    object More : Screen("more", R.string.nav_more)
    object Element : Screen("element", R.string.nav_element)
    object Attest : Screen("attest", R.string.nav_attest)
    object Claim : Screen("claim", R.string.nav_claim)
    object Result : Screen("result", R.string.nav_result)
}

@ExperimentalPermissionsApi
object NavUtils {
    /**
     * Top nav destinations for the application (Bottom nav locations)
     */
    private val bottomNavDestinations = listOf<Screen>(
        Screen.Home,
        Screen.Elements,
        Screen.Scanner,
        Screen.More,
    )

    /**
     * Provides a navigator for the application
     */
    @Composable
    fun Navigator() {
        val navController = rememberNavController()
        val showTopBar = remember { mutableStateOf(true) }
        val viewModel: AttestationViewModelImpl =
            viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)

        Scaffold(
            topBar = { if (showTopBar.value) TopBar(navController) },
            bottomBar = {
                BottomBar(navController)
            },
        ) { innerPadding ->
            NavHost(
                navController,
                startDestination = Screen.Home.route,
                Modifier.padding(innerPadding)
            ) {
                // Add new nav destinations here after Screen for it is created
                composable(Screen.Home.route) {
                    showTopBar.value = true; Home(navController, viewModel)
                }
                composable(Screen.Elements.route) {
                    showTopBar.value = true; Elements(navController, viewModel)
                }
                composable(Screen.Scanner.route) {
                    showTopBar.value = false; Scanner(navController)
                } // Experimental Permissions
                composable(Screen.More.route) { showTopBar.value = true; More(navController) }
                composable(Screen.Element.route) {
                    showTopBar.value = true; Element(navController, viewModel)
                }
                composable(Screen.Attest.route) {
                    showTopBar.value = true; Attest(navController, viewModel)
                }
                composable(Screen.Claim.route) {
                    showTopBar.value = true; Claim(navController, viewModel.useAttestationUtil())
                }
                composable(Screen.Result.route) {
                    showTopBar.value =
                        true; ResultScreenProvider(
                    navController = navController,
                    viewModel = viewModel,
                    resultFlow = viewModel.useAttestationUtil().result,
                )
                }
            }
        }
    }

    @Composable
    private fun BottomBar(navController: NavController) {
        BottomNavigation(backgroundColor = MaterialTheme.colors.secondary) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            bottomNavDestinations.forEach { screen ->
                BottomNavigationItem(
                    icon = {
                        Icon(getRouteIcon(screen), contentDescription = null)
                    },
                    label = { Text(stringResource(screen.stringResId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        if (screen.route != currentDestination?.route)
                            navController.navigate(screen.route) {
                                while (navController.popBackStack()) {
                                } // Remove backstack for back button

                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                    },

                    )
            }
        }
    }

    @Composable
    private fun TopBar(navController: NavController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val screen = currentRoute?.let { Screen.getScreenFromRoute(it) } ?: return
        val isTopDestination = bottomNavDestinations.contains(screen)

        // This avoids an empty space in the top bar if the back icon is not needed
        if (isTopDestination) {
            TopAppBar(
                title = { Text(stringResource(id = screen.stringResId)) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
            )
        } else {
            TopAppBar(
                title = { Text(stringResource(id = screen.stringResId)) },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                navigationIcon = {
                    IconButton(
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                                contentDescription = null,
                            )
                        },
                        onClick = { navController.navigateUp() },
                    )
                },
            )
        }
    }

    private fun getRouteIcon(screen: Screen): ImageVector {
        return when (screen) {
            Screen.Home -> Icons.Filled.Home
            Screen.Elements -> TablerIcons.DeviceDesktop
            Screen.Scanner -> TablerIcons.Qrcode
            Screen.More -> TablerIcons.Dots
            else -> TablerIcons.QuestionMark
        }
    }
}

// NavController extension that allows arguments
@SuppressLint("RestrictedApi") // ?
fun NavController.navigate(
    route: String,
    args: Bundle,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
) {
    val routeLink =
        NavDeepLinkRequest.Builder.fromUri(NavDestination.createRoute(route).toUri()).build()

    val deepLinkMatch = graph.matchDeepLink(routeLink)
    if (deepLinkMatch != null) {
        val destination = deepLinkMatch.destination
        val id = destination.id
        navigate(id, args, navOptions, navigatorExtras)
    } else {
        navigate(route, navOptions, navigatorExtras)
    }
}