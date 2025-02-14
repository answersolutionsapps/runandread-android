package com.answersolutions.runandread.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.ui.theme.largeSpace
import java.util.*

@Preview(showBackground = true)
@Composable
fun LanguagePickerDarkThemePreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        RunAndReadTheme(darkTheme = false) {
            LanguagePicker(
                selectedLanguage = Locale.getDefault(),
                availableLocales = listOf(
                    Locale.getDefault(),
                    Locale.FRANCE,
                    Locale.GERMAN,
                    Locale.ITALY
                ),
                onLanguageSelected = { _ -> },
                onDismiss = {}
            )
        }
    }
}


@Composable
fun LanguagePicker(
    selectedLanguage: Locale,
    availableLocales: List<Locale>,
    onLanguageSelected: (Locale) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val supportedLanguages = remember { availableLocales.sortedBy { it.displayName } }

    val selectedIndex = remember {
        supportedLanguages.indexOf(selectedLanguage)
    }
    val (selectedLanguageIndex, setSelectedLanguageIndex) = remember {
        mutableIntStateOf(selectedIndex)
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
                        text = "Select the main language of this book",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(largeSpace))
                LazyColumn {
                    items(supportedLanguages.size) { index ->
                        val language = supportedLanguages[index]
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    setSelectedLanguageIndex(index)
                                    val localLanguage = supportedLanguages[index]
                                    onLanguageSelected(localLanguage)
                                    onDismiss()
                                },
                            headlineContent = {
                                Text(
                                    language.getDisplayName(language),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingContent = {
                                if (index == selectedLanguageIndex) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        modifier = modifier.padding(vertical = largeSpace)
    )
}
