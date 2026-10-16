package com.somesh.appscout.navigation

sealed interface PaddockDestination {
    data object Home : PaddockDestination
    data object Calendar : PaddockDestination
    data object Standings : PaddockDestination
    data object Profile : PaddockDestination
    data class DriverDetail(val driverCode: String) : PaddockDestination
    data class RaceDetail(val round: Int) : PaddockDestination
}

enum class BottomTab(val label: String, val caption: String) {
    HOME("Home", "Weekend"),
    CALENDAR("Calendar", "Season"),
    STANDINGS("Standings", "Championship"),
    PROFILE("Profile", "My driver")
}

fun BottomTab.destination(): PaddockDestination = when (this) {
    BottomTab.HOME -> PaddockDestination.Home
    BottomTab.CALENDAR -> PaddockDestination.Calendar
    BottomTab.STANDINGS -> PaddockDestination.Standings
    BottomTab.PROFILE -> PaddockDestination.Profile
}

/** Which bottom tab should appear selected for a given destination. */
fun PaddockDestination.parentTab(): BottomTab = when (this) {
    PaddockDestination.Home -> BottomTab.HOME
    PaddockDestination.Calendar, is PaddockDestination.RaceDetail -> BottomTab.CALENDAR
    PaddockDestination.Standings, is PaddockDestination.DriverDetail -> BottomTab.STANDINGS
    PaddockDestination.Profile -> BottomTab.PROFILE
}

/** Detail screens are pushed on top of the tab shell and get a back affordance. */
val PaddockDestination.isDetail: Boolean
    get() = this is PaddockDestination.DriverDetail || this is PaddockDestination.RaceDetail
