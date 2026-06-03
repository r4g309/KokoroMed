package dev.r4g309.kokoromed.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.r4g309.kokoromed.BuildConfig
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.data.repository.AppTheme
@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        // ── Apariencia ────────────────────────────────────────────────────────
        SettingsCard(title = stringResource(R.string.settings_section_appearance)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(stringResource(R.string.settings_theme_title), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.settings_theme_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val options = listOf(
                    AppTheme.light  to stringResource(R.string.settings_theme_light),
                    AppTheme.system to stringResource(R.string.settings_theme_system),
                    AppTheme.dark   to stringResource(R.string.settings_theme_dark),
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { i, (theme, label) ->
                        SegmentedButton(
                            shape    = SegmentedButtonDefaults.itemShape(i, options.size),
                            selected = settings.theme == theme,
                            onClick  = { vm.setTheme(theme) },
                            icon     = {},
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // ── Estudio ───────────────────────────────────────────────────────────
        SettingsCard(title = stringResource(R.string.settings_section_study)) {
            SettingsRow(
                title    = stringResource(R.string.settings_timer_title),
                subtitle = stringResource(R.string.settings_timer_subtitle),
            ) {
                Switch(
                    checked         = settings.timer,
                    onCheckedChange = { vm.setTimer(it) },
                )
            }
        }

        // ── Acerca de ─────────────────────────────────────────────────────────
        SettingsCard(title = stringResource(R.string.settings_section_about)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.settings_about_tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        "v${BuildConfig.VERSION_NAME}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

// ── Componentes de layout ──────────────────────────────────────────────────────

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape  = MaterialTheme.shapes.large,
        color  = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(title,
                style    = MaterialTheme.typography.labelLarge,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    control: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier              = Modifier.weight(1f).padding(end = 16.dp),
            verticalArrangement   = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        control()
    }
}
