package com.doorvaani.navigation

/**
 * Navigation routes for DoorVaani Phase 0.
 * Sealed for type safety + easy extension (Vault, Shield, ContactDetail in Phase 1).
 * Matches charter: HomeDashboard, DialPad (keypad), Contacts, Settings.
 * Bottom nav order inspired by goldens (Home/Recents variant, Keypad, Contacts, Settings).
 * Deep link support e.g. from Home "Open Keypad" or quick call.
 */
sealed class DoorVaaniDestination(val route: String) {
    data object Splash : DoorVaaniDestination("splash")
    data object Home : DoorVaaniDestination("home")
    data object DialPad : DoorVaaniDestination("dialpad") {
        const val ROUTE_WITH_NUMBER = "dialpad/{number}"
        fun createRoute(number: String? = null) = if (number != null) "dialpad/$number" else "dialpad"
    }
    data object Contacts : DoorVaaniDestination("contacts")
    data object Settings : DoorVaaniDestination("settings")
    data object ActiveCall : DoorVaaniDestination("activecall") {
        const val ROUTE = "activecall/{number}"
        fun createRoute(number: String) = "activecall/$number"
    }
    data object IncomingCall : DoorVaaniDestination("incomingcall") {
        const val ROUTE = "incomingcall/{number}"
        fun createRoute(number: String) = "incomingcall/$number"
    }
    data object Vault : DoorVaaniDestination("vault")
    data object Shield : DoorVaaniDestination("shield")
    data object ContactDetail : DoorVaaniDestination("contact_detail") {
        const val ROUTE = "contact_detail/{id}"
        fun createRoute(id: String) = "contact_detail/$id"
    }

    companion object {
        val bottomNavDestinations = listOf(Home, DialPad, Contacts, Settings)
    }
}
