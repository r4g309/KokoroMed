package dev.r4g309.kokoromed.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.ui.navigation.*
import dev.r4g309.kokoromed.ui.theme.TealTint
import dev.r4g309.kokoromed.ui.theme.extended

private enum class Tab(@StringRes val labelRes: Int, val icon: Int) {
    LIBRARY(R.string.tab_library,   R.drawable.books),
    TODAY(R.string.tab_today,       R.drawable.calendar_today_24px),
    PROGRESS(R.string.tab_progress, R.drawable.bar_chart_24px),
    SETTINGS(R.string.tab_settings, R.drawable.settings_24px),
}

private fun NavDestination?.isSelected(tab: Tab): Boolean {
    if (this == null) return false
    return when (tab) {
        Tab.LIBRARY  -> hasRoute<Library>() || hasRoute<DeckDetail>()
        Tab.TODAY    -> hasRoute<Today>()
        Tab.PROGRESS -> hasRoute<Progress>()
        Tab.SETTINGS -> hasRoute<Settings>()
    }
}

private fun Tab.destination(): Any = when (this) {
    Tab.LIBRARY  -> Library
    Tab.TODAY    -> Today
    Tab.PROGRESS -> Progress
    Tab.SETTINGS -> Settings
}

@Composable
fun KokoroBottomBar(
    currentDestination: NavDestination?,
    dueCount: Int,
    onNavigate: (Any) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        windowInsets   = WindowInsets(0),
    ) {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentDestination.isSelected(tab),
                onClick  = { onNavigate(tab.destination()) },
                icon = {
                    val label = stringResource(tab.labelRes)
                    BadgedBox(badge = {
                        if (tab == Tab.TODAY && dueCount > 0) {
                            Badge {
                                Text(if (dueCount > 99) "99+" else dueCount.toString())
                            }
                        }
                    }) {
                        Icon(
                            painterResource(tab.icon),
                            contentDescription = label,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
                label  = { Text(stringResource(tab.labelRes), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    indicatorColor      = TealTint,
                    unselectedIconColor = MaterialTheme.extended.textSubtle,
                    unselectedTextColor = MaterialTheme.extended.textSubtle,
                ),
            )
        }
    }
}
