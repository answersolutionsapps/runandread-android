package com.answersolutions.runandread.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.ui.theme.largeSpace
import com.answersolutions.runandread.voice.RunAndReadVoice
import com.answersolutions.runandread.voice.languageId
import java.util.Locale

@Preview(showBackground = true)
@Composable
fun VoicePickerPreview() {
    RunAndReadTheme {
        VoicePicker(
            selectedLanguage = Locale.ENGLISH,
            availableVoices = listOf(
                RunAndReadVoice("Voice 1", "en"),
                RunAndReadVoice("Voice 2", "en"),
                RunAndReadVoice("Voice 3", "en"),
                RunAndReadVoice("Voice 4", "en")

            ),
            defaultVoice = RunAndReadVoice("Voice 2", "en"),
            onVoiceSelected = {},
            onSave = {},
            onDismiss = {}
        )
    }
}


@Composable
fun VoicePicker(
    selectedLanguage: Locale,
    availableVoices: List<RunAndReadVoice>,
    defaultVoice: RunAndReadVoice,
    onVoiceSelected: (RunAndReadVoice) -> Unit,
    onSave: (RunAndReadVoice) -> Unit,
    onDismiss: () -> Unit
) {
    val supportedVoices = remember {
        availableVoices.filter {
            it.locale.languageId() == selectedLanguage.languageId()
        }
    }

    val selectedVoice = remember {
        mutableStateOf(defaultVoice)
    }

    fun isSelected(voice: RunAndReadVoice): Boolean {
       return voice.name == selectedVoice.value.name
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = { onSave(selectedVoice.value) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        title = {

        },
        text = {
            Column {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Select Voice",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(largeSpace))
                LazyColumn {
                    items(supportedVoices.size) { index ->
                        val voice = supportedVoices[index]
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected(voice)) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = (isSelected(voice))) {
                                    selectedVoice.value = supportedVoices[index]
                                    onVoiceSelected(selectedVoice.value)
                                },
                            headlineContent = {
                                Text(
                                    voice.name,
                                    color = if (isSelected(voice)) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = "Play Sample",
                                    tint = if (isSelected(voice)) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        modifier = Modifier.padding(vertical = largeSpace)
    )
}
