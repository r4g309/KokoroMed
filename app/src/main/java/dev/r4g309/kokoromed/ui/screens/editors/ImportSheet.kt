package dev.r4g309.kokoromed.ui.screens.editors

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.r4g309.kokoromed.R
import dev.r4g309.kokoromed.ui.theme.Danger

private val SAMPLE = """{
  "deck": {
    "name": "Mi mazo",
    "description": "Descripción opcional",
    "color": "#0d9488",
    "icon": "stack"
  },
  "cards": [
    {
      "question": "¿Pregunta de ejemplo?",
      "options": ["Opción A", "Opción B", "Opción C", "Opción D"],
      "correct": 0,
      "explanation": "Por qué A es la correcta.",
      "tags": ["tema"],
      "difficulty": "medium"
    }
  ]
}"""

private val VALID_ICONS = listOf(
    "heart", "pill", "droplet", "brain", "stack", "layers", "stethoscope", "trophy"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSheet(
    onDismiss: () -> Unit,
    onImport: (raw: String) -> Unit,
) {
    val context = LocalContext.current
    var text  by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
        }.onSuccess { text = it; error = "" }
         .onFailure { error = context.getString(R.string.import_error_read_file) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(stringResource(R.string.import_title),
                    style = MaterialTheme.typography.titleLarge)
            }

            item {
                Text(
                    stringResource(R.string.import_instructions, VALID_ICONS.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick  = { filePicker.launch("*/*") },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(painterResource(R.drawable.upload_24px), null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.import_upload_button),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(
                        onClick  = { text = SAMPLE; error = "" },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.import_template_button),
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            item {
                OutlinedTextField(
                    value         = text,
                    onValueChange = { text = it; error = "" },
                    modifier      = Modifier.fillMaxWidth().height(240.dp),
                    placeholder   = {
                        Text(SAMPLE, style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace))
                    },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace),
                    shape   = MaterialTheme.shapes.medium,
                    isError = error.isNotBlank(),
                )
            }

            if (error.isNotBlank()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painterResource(R.drawable.close_24px), null,
                            tint = Danger, modifier = Modifier.size(15.dp))
                        Text(error, style = MaterialTheme.typography.bodySmall, color = Danger)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(
                        onClick  = { onImport(text.trim()) },
                        enabled  = text.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(painterResource(R.drawable.upload_24px), null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_import))
                    }
                }
            }
        }
    }
}
