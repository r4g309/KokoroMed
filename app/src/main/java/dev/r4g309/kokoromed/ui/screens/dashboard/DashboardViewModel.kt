package dev.r4g309.kokoromed.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.r4g309.kokoromed.data.db.ActivityEntity
import dev.r4g309.kokoromed.data.repository.DeckRepository
import dev.r4g309.kokoromed.data.repository.DeckWithProgress
import dev.r4g309.kokoromed.domain.model.todayKey
import dev.r4g309.kokoromed.domain.srs.computeStreak
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DashboardState(
    val decks: List<DeckWithProgress> = emptyList(),
    val activity: Map<String, Int>    = emptyMap(),
    val streak: Int                   = 0,
    val todayCount: Int               = 0,
    val weekCount: Int                = 0,
    val totalDays: Int                = 0,
    val totalReviews: Int             = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: DeckRepository,
) : ViewModel() {

    // 6 meses de historial para el heatmap
    private val heatmapFrom = run {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, -26)
        todayKey(cal.time)
    }

    val state = combine(
        repo.observeDecks(),
        repo.observeActivity(heatmapFrom),
    ) { decks, actList ->
        val activity = actList.associate { it.date to it.count }
        val today = todayKey()
        val weekCount = (0..6).sumOf { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            activity[todayKey(cal.time)] ?: 0
        }
        DashboardState(
            decks        = decks,
            activity     = activity,
            streak       = computeStreak(activity),
            todayCount   = activity[today] ?: 0,
            weekCount    = weekCount,
            totalDays    = activity.count { it.value > 0 },
            totalReviews = activity.values.sum(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())
}
