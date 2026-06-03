package dev.r4g309.kokoromed.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.r4g309.kokoromed.data.repository.AppTheme
import dev.r4g309.kokoromed.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
) : ViewModel() {

    val settings = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            dev.r4g309.kokoromed.data.repository.AppSettings())

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { repo.setTheme(theme) }
    }

    fun setTimer(enabled: Boolean) {
        viewModelScope.launch { repo.setTimer(enabled) }
    }
}
