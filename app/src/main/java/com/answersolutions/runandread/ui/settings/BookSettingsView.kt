package com.answersolutions.runandread.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.answersolutions.runandread.data.model.AudioBook
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.RunAndReadBook
import com.answersolutions.runandread.data.model.TextPart
import com.answersolutions.runandread.ui.components.NiceButton
import com.answersolutions.runandread.ui.components.NiceButtonLarge
import com.answersolutions.runandread.ui.settings.components.ConfirmDeleteDialog
import com.answersolutions.runandread.ui.settings.components.ErrorMessageDialog
import com.answersolutions.runandread.ui.settings.components.HorizontalPageListView
import com.answersolutions.runandread.ui.settings.components.LanguagePicker
import com.answersolutions.runandread.ui.settings.components.SpeechSpeedSelector
import com.answersolutions.runandread.ui.settings.components.VoicePicker
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.ui.theme.largeSpace
import com.answersolutions.runandread.ui.theme.scTypography
import com.answersolutions.runandread.ui.theme.smallSpace
import com.answersolutions.runandread.ui.theme.normalSpace
import com.answersolutions.runandread.voice.RunAndReadVoice
import com.answersolutions.runandread.voice.VoiceSelectorViewModel
import com.answersolutions.runandread.voice.toLocale
import com.answersolutions.runandread.voice.toVoice
import timber.log.Timber
import java.util.Locale

@Preview(showBackground = true)
@Composable
fun BookSettingsPreviewBook() {
    RunAndReadTheme(darkTheme = false) {
        BookSettingsScreenContent(
            loading = false,
            showVoiceError = false,
            bookState = BookSettingsViewModel.BookUIState(
                book = RunAndReadBook.sampleBooks().last(),
                title = RunAndReadBook.sampleBooks().last().title,
                author = RunAndReadBook.sampleBooks().last().author
            ),
            contextText = listOf(
                "With this approach, you can now have selectable text in your view without allowing the user to modify the content. The text will be fully selectable, and users will be able to copy it to the clipboard by selecting and using the standard copy commands.",
                "Lorem ipsum2",
                "Lorem ipsum3"
            ),
            selectedPage = 0,
            selectedLanguage = Locale.getDefault(),
            availableLocales = listOf(),
            recentLocales = listOf(),
            availableVoices = listOf(),
            selectedVoice = RunAndReadVoice("Voice 1", "en"),
            selectedRate = 1f,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookSettingsPreviewAudiobook() {
    RunAndReadTheme(darkTheme = true) {
        BookSettingsScreenContent(
            loading = false,
            showVoiceError = false,
            bookState = BookSettingsViewModel.BookUIState(
                book = AudioBook(
                    id = "0",
                    title = "Moby Dick",
                    author = "Herman Melville",
                    language = "en_GB",
                    voiceRate = 1.25f,
                    parts = listOf(
                        TextPart(0, "Call me Ishmael."),
                        TextPart(1, "Call me Ishmael.")
                    ),
                    lastPosition = 0,
                    updated = System.currentTimeMillis(),
                    audioFilePath = "",
                    voice = "Kokoko",
                    model = "Male, George",
                    bookSource = "www.gutenberg.org"
                ),
                title = RunAndReadBook.sampleBooks().first().title,
                author = RunAndReadBook.sampleBooks().first().author
            ),
            contextText = listOf(
                "With this approach, you can now have selectable text in your view without allowing the user to modify the content. The text will be fully selectable, and users will be able to copy it to the clipboard by selecting and using the standard copy commands.",
                "Lorem ipsum2",
                "Lorem ipsum3"
            ),
            selectedPage = 0,
            selectedLanguage = Locale.getDefault(),
            availableLocales = listOf(),
            recentLocales = listOf(),
            availableVoices = listOf(),
            selectedVoice = RunAndReadVoice("Voice 1", "en"),
            selectedRate = 1f,
            onEvent = {}
        )
    }
}

@Composable
fun BookSettingsScreenView(
    onBookDeleted: () -> Unit,
    onNavigateBack: (RunAndReadBook?) -> Unit,
    viewModel: BookSettingsViewModel,
    voiceSelector: VoiceSelectorViewModel
) {
    val voices by voiceSelector.availableVoices.collectAsState()
    val locales by voiceSelector.availableLocales.collectAsState()
    val recentLocales = viewModel.recentSelectionsL.values

    val bookState by viewModel.bookState.collectAsState()
    val viewState by viewModel.viewState.collectAsState()
    val showVoiceError = viewState.showVoiceError
    val contextText = bookState.text.ifEmpty {
        bookState.parts.map { it.text }
    }
    val selectedLanguage = bookState.language.toLocale()
    val selectedVoice = voiceSelector.nameToVoice(bookState.voiceIdentifier, bookState.language)
//    val selectedRate = bookState.voiceRate


    LaunchedEffect(Unit) {
        viewModel.setUpBook()
    }

    BookSettingsScreenContent(
        loading = viewState.loading,
        showVoiceError = showVoiceError,
        bookState = bookState,
        contextText = contextText,
        selectedPage = viewState.selectedPage,
        selectedLanguage = selectedLanguage,
        recentLocales = recentLocales.toList(),
        availableLocales = locales.toList(),
        availableVoices = voices.toList(),
        selectedRate = bookState.voiceRate,
        selectedVoice = selectedVoice,
        onEvent = { it.onEvent(viewModel) { onNavigateBack(bookState.book) } }
    )
    val pincode = remember { mutableStateOf("") }
    if (viewState.showDeleteDialog) {
        ConfirmDeleteDialog(
            pincode = pincode.value,
            buttonEnabled = (pincode.value == "delete"),
            onValueChange = { value ->
                pincode.value = value
            },
            onDeleteClicked = {
                if (pincode.value == "delete") {
                    viewModel.onDelete(onBookDeleted)
                }
            },
            onDismissRequest = {
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSettingsScreenContent(
    loading: Boolean,
    showVoiceError: Boolean,
    bookState: BookSettingsViewModel.BookUIState,
    contextText: List<String>,
    selectedPage: Int,
    recentLocales: List<String>,
    availableLocales: List<Locale>,
    availableVoices: List<RunAndReadVoice>,
    selectedVoice: RunAndReadVoice,
    selectedRate: Float,
    selectedLanguage: Locale,
    onEvent: (BookSettingsEvent) -> Unit
) {
    Box(Modifier.background(colorScheme.background)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    actions = {
                        TextButton (onClick = {onEvent(BookSettingsEvent.Cancel)}) {
                            Text("Cancel",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp))
                        }
                        Spacer(modifier = Modifier.weight(1F))
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.weight(1F))
                        TextButton (onClick = {onEvent(BookSettingsEvent.Save)}) {
                            Text("Save",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp))
                        }


                    }
                )
            },
            content = { padding ->
                Column(
                    Modifier
                        .padding(padding)
                        .padding(largeSpace)
                        .verticalScroll(rememberScrollState())
                ) {
                    val showLanguageDialog = remember { mutableStateOf(false) }
                    val showVoiceDialog = remember { mutableStateOf(false) }

                    BookTitleSection(
                        title = bookState.title,
                        author = bookState.author,
                        onTitleChanged = { onEvent(BookSettingsEvent.TitleChanged(it)) },
                        onAuthorChanged = { onEvent(BookSettingsEvent.AuthorChanged(it)) }
                    )
                    Spacer(Modifier.height(normalSpace))
                    HorizontalDivider(color = colorScheme.onSurface)
                    Spacer(Modifier.height(normalSpace))
                    Column(
                        modifier = Modifier
                            .padding()
                    ) {
                        if (bookState.book is Book) {
                            Text(
                                text = "Selected Language",
                                style = scTypography.titleMedium,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(smallSpace))
                            NiceButton(
                                title = selectedLanguage.displayName,
                                clickHandler = {
                                    showLanguageDialog.value = true
                                },
                            )
                        } else if (bookState.book is AudioBook){
                            val book = bookState.book
                            Text(
                                text = "Book's Language",
                                style = scTypography.titleMedium,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(smallSpace))
                            val locale = book.language.toLocale()
                            Text(
                                text = "${locale.displayLanguage} (${locale.displayCountry})",
                                style = scTypography.titleLarge,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = smallSpace)
                            )
                        }

                        Spacer(Modifier.height(normalSpace))
                        SpeechSpeedSelector(
                            defaultSpeed = selectedRate,
                            onSpeedSelected = { onEvent(BookSettingsEvent.SpeedSelected(it)) })
                        Spacer(Modifier.height(normalSpace))
                        Text(
                            text = "Selected Voice",
                            style = scTypography.titleMedium,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(smallSpace))
                        Row {
                            if (bookState.book is Book) {
                                NiceButton(
                                    title = selectedVoice.name,
                                    clickHandler = {
                                        showVoiceDialog.value = true
                                    },
                                )
                                Spacer(Modifier.width(smallSpace))
                            } else if (bookState.book is AudioBook){
                                val book = bookState.book
                                Text(
                                    text = book.model + "\n" + book.voice,
                                    style = scTypography.titleLarge,
                                    color = colorScheme.onSurface,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.padding(top = smallSpace)
                                )
                            }
                            IconButton(onClick = {
                                onEvent(
                                    if (bookState.book is Book) {
                                        BookSettingsEvent.PlayVoiceSample(
                                            selectedLanguage,
                                            selectedVoice.toVoice(),
                                            selectedRate
                                        )
                                    } else {
                                        BookSettingsEvent.PlayAudioSample
                                    }
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = "play/stop sample",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                        }
                        Spacer(Modifier.height(normalSpace))
                        HorizontalDivider(color = colorScheme.onSurface)
                        if (bookState.book is AudioBook) {
                            Spacer(Modifier.height(normalSpace))
                            val book = bookState.book
                            Text(
                                text = "Book source",
                                style = scTypography.titleMedium,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(smallSpace))
                            Text(
                                text = book.bookSource,
                                style = scTypography.titleLarge,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = smallSpace)
                            )
                            Spacer(Modifier.height(normalSpace))
                            HorizontalDivider(color = colorScheme.onSurface)
                        }
                        Spacer(Modifier.height(normalSpace))
                        if (bookState.book is Book) {
                            Text(
                                text = "Select number of Pages to skip",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        } else {
                            Text(
                                text = "Text Preview",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        HorizontalPageListView(
                            selectedPage = selectedPage,
                            totalPages = contextText.size,
                            onPageChanged = { onEvent(BookSettingsEvent.PageSelected(it)) }
                        )
                        Spacer(Modifier.height(normalSpace))
                        BasicTextField(
                            value = if (selectedPage <= contextText.size - 1) {
                                contextText[selectedPage]
                            } else "",
                            textStyle = TextStyle(
                                fontSize = 17.sp,
                                color = colorScheme.onSurface
                            ),
                            singleLine = false,
                            minLines = 10,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    colorScheme.background,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                    Spacer(Modifier.height(normalSpace))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(Modifier.height(normalSpace))
                    NiceButtonLarge(title = "Delete This Book", color = Color.Red) {
                        onEvent(BookSettingsEvent.DeleteClicked)
                    }
                    if (showLanguageDialog.value) {
                        LanguagePicker(defaultLanguage = selectedLanguage,
                            availableLocales = availableLocales,
                            recentLocales = recentLocales,
                            onLanguageSelected = {
                                onEvent(BookSettingsEvent.LanguageSelected(it))
                                Timber.d("onLanguageSelected=>$it")
                                showLanguageDialog.value = false
                            }, onDismiss = {
                                showLanguageDialog.value = false
                            })
                    } else if (showVoiceDialog.value) {
                        VoicePicker(selectedLanguage = selectedLanguage,
                            defaultVoice = selectedVoice,
                            availableVoices = availableVoices,
                            onVoiceSelected = {
                                onEvent(
                                    BookSettingsEvent.PlayVoiceSample(
                                        selectedLanguage,
                                        it.toVoice(),
                                        selectedRate
                                    )
                                )
                            },
                            onSave = {
                                onEvent(BookSettingsEvent.VoiceSelected(it))
                                showVoiceDialog.value = false
                            },
                            onDismiss = {
                                showVoiceDialog.value = false
                            })
                    }
                }
            }
        )
        if (loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xcF7f7f7f))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorScheme.primary
                )

            }
        }
        if (showVoiceError) {
            ErrorMessageDialog(
                title = "Voice Error",
                message = "Something went wrong. Please check your internet connection and try again. Go to Settings and check if Text-to-Speech and voices are available on your phone.",
                onDismissRequest = {
                    onEvent(BookSettingsEvent.DismissVoiceErrorDialog)
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookTitleSection(
    title: String,
    author: String,
    onTitleChanged: (String) -> Unit,
    onAuthorChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    focusManager.moveFocus(FocusDirection.Enter)

    Column {
        OutlinedTextField(
            value = title,
            placeholder = {
                Text(
                    text = "Input title",
                    color = Color.LightGray
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = colorScheme.onSurface
            ),
            onValueChange = onTitleChanged,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = colorScheme.surface,
                unfocusedBorderColor = colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (title.isNotEmpty()) {
                    IconButton(onClick = { onTitleChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear text",
                            tint = Color.Gray
                        )
                    }
                }
            }
        )
        Spacer(Modifier.height(normalSpace))
        OutlinedTextField(
            value = author,
            placeholder = {
                Text(
                    text = "Input author",
                    color = Color.LightGray
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = colorScheme.onSurface
            ),
            onValueChange = onAuthorChanged,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = colorScheme.surface,
                unfocusedBorderColor = colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (title.isNotEmpty()) {
                    IconButton(onClick = { onAuthorChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear text",
                            tint = Color.Gray
                        )
                    }
                }
            }
        )
    }
}