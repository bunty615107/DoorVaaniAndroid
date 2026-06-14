package com.doorvaani.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.doorvaani.R

/**
 * Bottom navigation bar matching Stitch goldens (home_dashboard_doorvaani/ + dial_pad_doorvaani/ + contacts_doorvaani/).
 * Icons + labels: Recents (History) / Keypad (Call) / Contacts / Settings.
 * Home tab hosts the full bento dashboard (per charter B-06). Active pill highlight on selected.
 * Uses design tokens (primaryContainer for active, onSurfaceVariant for idle).
 * Haptics on tab switch (caller provides).
 */
@Composable
fun DoorVaaniBottomNavBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    onTabSelected: (DoorVaaniDestination) -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        DoorVaaniDestination.bottomNavDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true

            val icon: ImageVector
            val label: String
            when (destination) {
                DoorVaaniDestination.Home -> {
                    icon = Icons.Filled.History
                    label = stringResource(R.string.recents)
                }
                DoorVaaniDestination.DialPad -> {
                    icon = Icons.Filled.Call
                    label = stringResource(R.string.keypad)
                }
                DoorVaaniDestination.Contacts -> {
                    icon = Icons.Filled.Contacts
                    label = stringResource(R.string.contacts_title)
                }
                DoorVaaniDestination.Settings -> {
                    icon = Icons.Filled.Settings
                    label = stringResource(R.string.settings_title)
                }
                else -> {
                    icon = Icons.Filled.History
                    label = "Home"
                }
            }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onTabSelected(destination)
                    navController.navigate(destination.route) {
                        // Pop up to start to avoid stack buildup on tab switches
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
