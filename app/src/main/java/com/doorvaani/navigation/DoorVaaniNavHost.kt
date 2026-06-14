package com.doorvaani.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.doorvaani.domain.model.CallState
import com.doorvaani.domain.repository.FakePreferencesRepository
import com.doorvaani.domain.repository.FakeVaultRepository
import com.doorvaani.ui.screens.*
import com.doorvaani.viewmodels.CallViewModel
import com.doorvaani.viewmodels.SimpleHomeViewModel
import com.doorvaani.domain.model.UserPreferences
import com.doorvaani.ui.theme.LocalDoorVaaniTheme

/**
 * Root NavHost + Scaffold with BottomNav.
 * Phase 0 architecture owner.
 * Splash is initial; transitions to Home.
 * Deep links: DialPad can receive pre-filled number from Home Quick Dial card.
 * Call flow: DialPad onCall -> CallViewModel.dial() -> navigate to ActiveCall.
 * Theme switcher lives in Settings (global state via prefs repo for now).
 *
 * Bottom nav hidden on Splash and full-screen transactional (ActiveCall stub).
 */
@Composable
fun DoorVaaniNavHost(
    modifier: Modifier = Modifier,
    startInSplash: Boolean = true
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current

    // Init encrypted vault early (Agent C)
    LaunchedEffect(Unit) {
        FakeVaultRepository.initWithContext(context)
    }

    // Central call state (Phase 0: simple VM + state hoisted here for nav decisions)
    val callViewModel = remember { CallViewModel() }
    val callState by callViewModel.callState.collectAsState()

    // Home / prefs (focus toggles + samples)
    val homeViewModel = remember { SimpleHomeViewModel(FakePreferencesRepository) }
    val prefs by homeViewModel.prefs.collectAsState()

    // Determine if bottom nav should show (hide on splash/active)
    val showBottomNav = currentDestination?.route?.let { route ->
        route != DoorVaaniDestination.Splash.route &&
                !route.startsWith(DoorVaaniDestination.ActiveCall.route.split("/")[0])
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                DoorVaaniBottomNavBar(
                    navController = navController,
                    currentDestination = currentDestination,
                    onTabSelected = { /* nav handled inside */ }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (startInSplash) DoorVaaniDestination.Splash.route else DoorVaaniDestination.Home.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(DoorVaaniDestination.Splash.route) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(DoorVaaniDestination.Home.route) {
                            popUpTo(DoorVaaniDestination.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(DoorVaaniDestination.Home.route) {
                HomeDashboardScreen(
                    prefs = prefs,
                    onToggleDharma = { homeViewModel.toggleDharma() },
                    onToggleVastu = { homeViewModel.toggleVastu() },
                    onOpenKeypad = {
                        navController.navigate(DoorVaaniDestination.DialPad.route)
                    },
                    onQuickCall = { number ->
                        // Deep action: go to dial or directly trigger stub call
                        navController.navigate(DoorVaaniDestination.DialPad.createRoute(number))
                    },
                    onNavigateToContacts = {
                        navController.navigate(DoorVaaniDestination.Contacts.route)
                    },
                    recentCalls = homeViewModel.getRecentCalls(),
                    weeklyFlow = homeViewModel.getWeeklyFlow()
                )
            }

            composable(
                route = DoorVaaniDestination.DialPad.ROUTE_WITH_NUMBER,
                arguments = listOf(navArgument("number") { nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val prefilled = backStackEntry.arguments?.getString("number")
                DialPadScreen(
                    initialNumber = prefilled ?: "",
                    onCall = { number ->
                        callViewModel.startCall(number, context)
                        navController.navigate(DoorVaaniDestination.ActiveCall.createRoute(number))
                    },
                    onAddContact = { /* stub: future contact detail */ },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(DoorVaaniDestination.DialPad.route) {
                DialPadScreen(
                    initialNumber = "",
                    onCall = { number ->
                        callViewModel.startCall(number, context)
                        navController.navigate(DoorVaaniDestination.ActiveCall.createRoute(number))
                    },
                    onAddContact = { /* TODO */ },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(DoorVaaniDestination.Contacts.route) {
                ContactsScreen(
                    onCallContact = { contact ->
                        callViewModel.startCall(contact.phone, context)
                        navController.navigate(DoorVaaniDestination.ActiveCall.createRoute(contact.phone))
                    },
                    onMessageContact = { /* stub */ },
                    onOpenDetail = { id ->
                        navController.navigate(DoorVaaniDestination.ContactDetail.createRoute(id))
                    }
                )
            }

            composable(DoorVaaniDestination.Settings.route) {
                SettingsScreen(
                    currentPrefs = prefs,
                    onThemeToggle = { isSangam ->
                        homeViewModel.setTheme(isSangam)
                        // Note: actual theme switch happens at root in MainActivity via observed state or recompose
                    },
                    onDharmaToggle = { homeViewModel.toggleDharma() },
                    onVastuToggle = { homeViewModel.toggleVastu() },
                    // Phase 3
                    onCloudSyncToggle = { homeViewModel.toggleCloudSync() },
                    onCommunityFederationToggle = { homeViewModel.toggleCommunityFederation() },
                    onPremiumAiToggle = { homeViewModel.togglePremiumAi() },
                    onNavigateToVault = { navController.navigate(DoorVaaniDestination.Vault.route) },
                    onNavigateToShield = { navController.navigate(DoorVaaniDestination.Shield.route) }
                )
            }

            composable(
                route = DoorVaaniDestination.ActiveCall.ROUTE,
                arguments = listOf(navArgument("number") { defaultValue = "" })
            ) { backStackEntry ->
                val number = backStackEntry.arguments?.getString("number") ?: ""
                val isMuted by callViewModel.isMuted.collectAsState()
                val isSpeaker by callViewModel.isSpeaker.collectAsState()
                val isOnHold by callViewModel.isOnHold.collectAsState()
                val isRecording by callViewModel.isRecording.collectAsState()

                ActiveCallScreen(
                    number = number,
                    callState = callState,
                    onEndCall = {
                        callViewModel.endCall()
                        navController.popBackStack(DoorVaaniDestination.Home.route, false)
                    },
                    onToggleMute = { callViewModel.toggleMute() },
                    onToggleSpeaker = { callViewModel.toggleSpeaker() },
                    onToggleHold = { callViewModel.toggleHold() },
                    onToggleRecord = { callViewModel.toggleRecording(context) },
                    onToggleKeypad = { /* stub: future in-call keypad overlay */ },
                    onAddCall = { /* stub: conference future */ },
                    onMerge = { /* stub */ },
                    isMuted = isMuted,
                    isSpeaker = isSpeaker,
                    isOnHold = isOnHold,
                    isRecording = isRecording
                )
            }

            composable(
                route = DoorVaaniDestination.IncomingCall.ROUTE,
                arguments = listOf(navArgument("number") { defaultValue = "" })
            ) { backStackEntry ->
                val number = backStackEntry.arguments?.getString("number") ?: ""
                IncomingCallScreen(
                    number = number,
                    onAccept = {
                        callViewModel.acceptIncoming(number)
                        navController.navigate(DoorVaaniDestination.ActiveCall.createRoute(number))
                    },
                    onDecline = {
                        navController.popBackStack(DoorVaaniDestination.Home.route, false)
                    }
                )
            }

            composable(DoorVaaniDestination.Vault.route) {
                // Phase 1 Vault (Agent C): uses FakeVaultRepository (EncryptedVaultStore + RecordingCoordinator data).
                // Legacy archived from VM kept for compat but new screen ignores (real encrypted records from call end hooks).
                // Phase 3: pass premiumAiEnabled for conditional insights display.
                VaultScreen(premiumAiEnabled = prefs.premiumAiEnabled)
            }

            composable(DoorVaaniDestination.Shield.route) {
                ShieldScreen(
                    federationEnabled = prefs.communityFederationEnabled,
                    onToggleFederation = { homeViewModel.toggleCommunityFederation() }
                )
            }

            composable(
                route = DoorVaaniDestination.ContactDetail.ROUTE,
                arguments = listOf(navArgument("id") { defaultValue = "" })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                ContactDetailScreen(contactId = id)
            }
        }
    }
}
