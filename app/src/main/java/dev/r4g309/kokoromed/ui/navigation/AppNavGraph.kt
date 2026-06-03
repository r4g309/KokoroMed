package dev.r4g309.kokoromed.ui.navigation

import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.r4g309.kokoromed.R
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.r4g309.kokoromed.data.db.CardEntity
import dev.r4g309.kokoromed.data.db.DeckEntity
import dev.r4g309.kokoromed.domain.srs.DeckProgress
import dev.r4g309.kokoromed.ui.components.KokoroBottomBar
import dev.r4g309.kokoromed.ui.screens.deck.DeckDetailScreen
import dev.r4g309.kokoromed.ui.screens.editors.CardEditorSheet
import dev.r4g309.kokoromed.ui.screens.editors.DeckEditorSheet
import dev.r4g309.kokoromed.ui.screens.editors.EditorsViewModel
import dev.r4g309.kokoromed.ui.screens.editors.ImportSheet
import dev.r4g309.kokoromed.ui.screens.library.LibraryScreen
import dev.r4g309.kokoromed.ui.screens.progress.ProgressScreen
import dev.r4g309.kokoromed.ui.screens.settings.SettingsScreen
import dev.r4g309.kokoromed.ui.screens.study.StudySessionScreen
import dev.r4g309.kokoromed.ui.screens.study.StudySetupSheet
import dev.r4g309.kokoromed.ui.screens.today.TodayScreen
import kotlinx.coroutines.launch

private data class SetupTarget(
    val deckId: String, val deckName: String, val progress: DeckProgress,
)

private sealed class EditorState {
    object Closed : EditorState()
    data class Deck(val deck: DeckEntity?) : EditorState()
    data class Card(val card: CardEntity?, val deckId: String) : EditorState()
    object Import : EditorState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(dueCount: Int, timer: Boolean = false) {
    val navController = rememberNavController()
    val currentDest   = navController.currentBackStackEntryAsState().value?.destination
    val context       = LocalContext.current

    val editorsVm: EditorsViewModel = hiltViewModel()
    var setupTarget by remember { mutableStateOf<SetupTarget?>(null) }
    var editorState by remember { mutableStateOf<EditorState>(EditorState.Closed) }

    val isStudyScreen = currentDest?.hasRoute<Study>() == true

    // Título del TopAppBar — estático por pantalla, dinámico en DeckDetail
    var deckDetailTitle by remember { mutableStateOf("") }
    val topBarTitle = when {
        currentDest?.hasRoute<Library>()    == true -> stringResource(R.string.tab_library)
        currentDest?.hasRoute<Today>()      == true -> stringResource(R.string.tab_today)
        currentDest?.hasRoute<Progress>()   == true -> stringResource(R.string.tab_progress)
        currentDest?.hasRoute<Settings>()   == true -> stringResource(R.string.tab_settings)
        currentDest?.hasRoute<DeckDetail>() == true -> deckDetailTitle
        else -> ""
    }
    val showBack = currentDest?.hasRoute<DeckDetail>() == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isStudyScreen) {
                TopAppBar(
                    title = {
                        Text(topBarTitle,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    },
                    navigationIcon = {
                        if (showBack) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(R.drawable.keyboard_arrow_right_24px),
                                    contentDescription = stringResource(R.string.action_back),
                                    modifier = Modifier.rotate(180f),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor    = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        },
        bottomBar = {
            if (!isStudyScreen) {
                KokoroBottomBar(
                    currentDestination = currentDest,
                    dueCount           = dueCount,
                    onNavigate         = { dest ->
                        navController.navigate(dest) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController       = navController,
            startDestination    = Library,
            modifier            = Modifier.padding(innerPadding),
            enterTransition     = { slideInHorizontally { it / 4 } + fadeIn() },
            exitTransition      = { slideOutHorizontally { -it / 4 } + fadeOut() },
            popEnterTransition  = { slideInHorizontally { -it / 4 } + fadeIn() },
            popExitTransition   = { slideOutHorizontally { it / 4 } + fadeOut() },
        ) {
            composable<Library> {
                LibraryScreen(
                    onOpenDeck  = { id -> navController.navigate(DeckDetail(id)) },
                    onStudyDeck = { id, name, progress ->
                        setupTarget = SetupTarget(id, name, progress)
                    },
                    onNewDeck = { editorState = EditorState.Deck(null) },
                    onImport  = { editorState = EditorState.Import },
                )
            }
            composable<DeckDetail> {
                DeckDetailScreen(
                    onBack        = { navController.popBackStack() },
                    onTitleChange = { deckDetailTitle = it },
                    onStudy = { id, name, progress ->
                        setupTarget = SetupTarget(id, name, progress)
                    },
                    onEditDeck   = { deck -> editorState = EditorState.Deck(deck) },
                    onAddCard    = { deckId -> editorState = EditorState.Card(null, deckId) },
                    onEditCard   = { card -> editorState = EditorState.Card(card, card.deckId) },
                    onDeleteCard = { cardId ->
                        editorsVm.viewModelScope.launch { editorsVm.deleteCard(cardId) }
                    },
                    onDeleteDeck = { deckId ->
                        editorsVm.viewModelScope.launch {
                            editorsVm.deleteDeck(deckId)
                            navController.popBackStack()
                        }
                    },
                )
            }
            composable<Study> {
                StudySessionScreen(onExit = { navController.popBackStack() }, timer = timer)
            }
            composable<Today> {
                TodayScreen(onStudyDeck = { id, name, progress ->
                    setupTarget = SetupTarget(id, name, progress)
                })
            }
            composable<Progress> {
                ProgressScreen(onOpenDeck = { id -> navController.navigate(DeckDetail(id)) })
            }
            composable<Settings> { SettingsScreen() }
        }
    }

    // ── Study setup sheet ─────────────────────────────────────────────────────
    setupTarget?.let { target ->
        StudySetupSheet(
            deckName  = target.deckName,
            progress  = target.progress,
            onDismiss = { setupTarget = null },
            onStart   = { mode, maxQuestions ->
                setupTarget = null
                navController.navigate(Study(target.deckId, mode.name, maxQuestions ?: 0))
            },
        )
    }

    // ── Editor sheets ─────────────────────────────────────────────────────────
    when (val es = editorState) {
        is EditorState.Deck -> DeckEditorSheet(
            deck      = es.deck,
            onDismiss = { editorState = EditorState.Closed },
            onSave    = { name, desc, color, icon ->
                editorsVm.viewModelScope.launch {
                    editorsVm.saveDeck(es.deck, name, desc, color, icon)
                    editorState = EditorState.Closed
                    Toast.makeText(context,
                        context.getString(if (es.deck != null) R.string.toast_deck_updated else R.string.toast_deck_created),
                        Toast.LENGTH_SHORT).show()
                }
            },
        )
        is EditorState.Card -> CardEditorSheet(
            card      = es.card,
            onDismiss = { editorState = EditorState.Closed },
            onSave    = { question, options, correct, explanation, tags, difficulty ->
                editorsVm.viewModelScope.launch {
                    editorsVm.saveCard(es.deckId, es.card,
                        question, options, correct, explanation, tags, difficulty)
                    editorState = EditorState.Closed
                    Toast.makeText(context,
                        context.getString(if (es.card != null) R.string.toast_card_updated else R.string.toast_card_added),
                        Toast.LENGTH_SHORT).show()
                }
            },
        )
        is EditorState.Import -> ImportSheet(
            onDismiss = { editorState = EditorState.Closed },
            onImport  = { raw ->
                val fallbackName = context.getString(R.string.import_deck_fallback_name)
                editorsVm.viewModelScope.launch {
                    editorsVm.importDeck(raw, fallbackName)
                        .onSuccess { msg ->
                            editorState = EditorState.Closed
                            Toast.makeText(context, context.getString(R.string.toast_imported, msg),
                                Toast.LENGTH_SHORT).show()
                        }
                        .onFailure { e ->
                            Toast.makeText(context, context.getString(R.string.toast_invalid_json, e.message.orEmpty()),
                                Toast.LENGTH_LONG).show()
                        }
                }
            },
        )
        EditorState.Closed -> Unit
    }
}
