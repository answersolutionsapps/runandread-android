package com.answersolutions.runandread.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.answersolutions.runandread.voice.languageId
import java.util.*

@Preview(showBackground = true)
@Composable
fun LanguagePickerDarkThemePreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        RunAndReadTheme(darkTheme = false) {
            LanguagePicker(
                defaultLanguage = Locale.getDefault(),
                availableLocales = listOf(
                    Locale.getDefault(),
                    Locale.CANADA_FRENCH,
                    Locale.KOREAN,
                    Locale.FRANCE,
                    Locale.CHINA,
                    Locale.GERMANY,
                    Locale.ITALY
                ),
                recentLocales= listOf("en_US", "de_DE"),
                onLanguageSelected = { _ -> },
                onDismiss = {}
            )
        }
    }
}

@Composable
fun LanguagePicker(
    defaultLanguage: Locale,
    availableLocales: List<Locale>,
    recentLocales: List<String>,
    onLanguageSelected: (Locale) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {

    val supportedLanguages = remember { availableLocales.sortedBy { it.displayName } }
    val selectedLanguage = remember {
        mutableStateOf(defaultLanguage)
    }

    fun isSelected(language: Locale): Boolean {
        return language.languageId() == selectedLanguage.value.languageId()
    }
    val sortedLanguages = supportedLanguages.sortedBy { language ->
        val index = recentLocales.indexOf(language.languageId()) // Check if it's in recent
        if (index != -1) index else Int.MAX_VALUE // Prioritize recent, others go below
    }

    AlertDialog(
        onDismissRequest = {onDismiss()},
        confirmButton = {},
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
                        text = "Select the book's language",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(largeSpace))

                LazyColumn {
                    items(sortedLanguages.size) { index ->
                        val language = sortedLanguages[index]
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected(language)) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = isSelected(language)) {
                                    selectedLanguage.value = language
                                    onLanguageSelected(language)
                                    onDismiss()
                                },
                            headlineContent = {
                                Text(
                                    language.getDisplayName(language),
                                    color = if (isSelected(language)) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            },
                            trailingContent = {
                                if (isSelected(language)) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected Language",
                                        tint = if (isSelected(language)) {
                                            MaterialTheme.colorScheme.surface
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }

//                LazyColumn {
//                    items(supportedLanguages.size) { index ->
//                        val language = supportedLanguages[index]
//                        ListItem(
//                            colors = ListItemDefaults.colors(
//                                containerColor = if (isSelected(language)) {
//                                    MaterialTheme.colorScheme.primary
//                                } else {
//                                    MaterialTheme.colorScheme.surface
//                                }
//                            ),
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .selectable(selected = (isSelected(language))) {
//                                    selectedLanguage.value = language
//
//                                    val localLanguage = supportedLanguages[index]
//                                    onLanguageSelected(localLanguage)
//                                    onDismiss()
//                                },
//                            headlineContent = {
//                                Text(
//                                    language.getDisplayName(language),
//                                    color = if (isSelected(language)) {
//                                        MaterialTheme.colorScheme.surface
//                                    } else {
//                                        MaterialTheme.colorScheme.primary
//                                    }
//                                )
//                            },
//                            trailingContent = {
//                                if (language.language == selectedLanguage.value.language) {
//                                    Icon(
//                                        imageVector = Icons.Default.Check,
//                                        contentDescription = "Selected Language",
//                                        tint = if (isSelected(language)) {
//                                            MaterialTheme.colorScheme.surface
//                                        } else {
//                                            MaterialTheme.colorScheme.primary
//                                        }
//                                    )
//                                }
//                            }
//                        )
//                        HorizontalDivider()
//                    }
//                }
            }
        },
        modifier = modifier.padding(vertical = largeSpace)
    )
}
