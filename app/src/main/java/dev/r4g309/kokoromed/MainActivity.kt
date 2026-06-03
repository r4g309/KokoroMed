package dev.r4g309.kokoromed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.r4g309.kokoromed.data.repository.AppSettings
import dev.r4g309.kokoromed.data.repository.AppTheme
import dev.r4g309.kokoromed.data.repository.DeckRepository
import dev.r4g309.kokoromed.data.repository.SettingsRepository
import dev.r4g309.kokoromed.ui.navigation.AppNavGraph
import dev.r4g309.kokoromed.ui.theme.KokoroMedTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppUiState(
    val dueCount: Int     = 0,
    val settings: AppSettings = AppSettings(),
)

@HiltViewModel
class MainViewModel @Inject constructor(
    repo: DeckRepository,
    settingsRepo: SettingsRepository,
) : ViewModel() {
    val uiState = combine(
        repo.observeTotalDue(),
        settingsRepo.settings,
    ) { due, settings ->
        AppUiState(dueCount = due, settings = settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by vm.uiState.collectAsStateWithLifecycle()
            val isDark = when (state.settings.theme) {
                AppTheme.dark   -> true
                AppTheme.light  -> false
                AppTheme.system -> isSystemInDarkTheme()
            }
            KokoroMedTheme(darkTheme = isDark) {
                AppNavGraph(
                    dueCount = state.dueCount,
                    timer    = state.settings.timer,
                )
            }
        }
    }
}
