package com.answersolutions.runandread

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.answersolutions.runandread.ui.navigation.NavigationViewModel
import com.answersolutions.runandread.ui.about.AboutScreen
import com.answersolutions.runandread.ui.library.LibraryScreenView
import com.answersolutions.runandread.ui.library.LibraryScreenViewModel
import com.answersolutions.runandread.ui.init.SplashScreenView
import com.answersolutions.runandread.ui.player.PlayerScreenView
import com.answersolutions.runandread.ui.player.PlayerViewModel
import com.answersolutions.runandread.ui.settings.BookSettingsScreenView
import com.answersolutions.runandread.ui.settings.BookSettingsViewModel
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.voice.VoiceSelectorViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

sealed class Screen(val route: String) {
    data object Splash : Screen("init")
    data object Home : Screen("home")
    data object BookSettings : Screen("settings")
    data object About : Screen("about")
    data object Player : Screen("player")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val navigationViewModel by viewModels<NavigationViewModel>()
    private val libraryViewModel by viewModels<LibraryScreenViewModel>()
    private val playerViewModel by viewModels<PlayerViewModel>()
    private val bookSettingsViewModel by viewModels<BookSettingsViewModel>()
    private val voiceSelectorViewModel by viewModels<VoiceSelectorViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RunAndRead)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "false")
        }
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            // Observe navigation events
            LaunchedEffect(Unit) {
                navigationViewModel.onNavigationEvents(navController)
            }
            LaunchedEffect("init") {
                voiceSelectorViewModel.loadVoices()
                libraryViewModel.loadBooks()
            }
            RunAndReadTheme {
                NavHost(navController, startDestination = Screen.Splash.route) {
                    composable(Screen.Splash.route) {
                        SplashScreenView(onNavigate = { screen ->
                            libraryViewModel.selectedBook.value?.let {
                                //todo: do not pass book, rather load it in viewmodel by repository
                                playerViewModel.setUpBook(it)
                            }
                            navigationViewModel.navigateTo(screen)
                        }, libraryViewModel)
                    }
                    composable(Screen.Home.route) {
                        LibraryScreenView(
                            viewModel = libraryViewModel,
                            onSelect = { book ->
                                libraryViewModel.onSelectBook(book)
                                //todo: do not pass book, rather load it in viewmodel by repository
                                playerViewModel.setUpBook(book)
                                navigationViewModel.navigateTo(Screen.Player)
                            },
                            onAboutClicked = {
                                navigationViewModel.navigateTo(Screen.About)
                            },
                            onFileSelected = {
                                bookSettingsViewModel.createANewBook(it)
                                navigationViewModel.navigateTo(Screen.BookSettings)
                            }
                        )
                    }
                    composable(Screen.BookSettings.route) {
                        BookSettingsScreenView(
                            onNavigateBack = { book ->
                                if (book == null) {
                                    navigationViewModel.popBack()
                                } else {
                                    libraryViewModel.onSelectBook(book)
                                    //todo: do not pass book, rather load it in viewmodel by repository
                                    playerViewModel.setUpBook(book)
                                    navigationViewModel.navigateTo(Screen.Player)
                                }
                            },
                            viewModel = bookSettingsViewModel,
                            voiceSelector = voiceSelectorViewModel
                        )
                    }
                    composable(Screen.About.route) {
                        AboutScreen {
                            navigationViewModel.popBack()
                        }
                    }
                    composable(Screen.Player.route) {
                        PlayerScreenView(
                            onBackToLibrary = {
                                libraryViewModel.onUnselectBook()
                                navigationViewModel.navigateTo(Screen.Home)
                            }, onSettings = {
                                navigationViewModel.navigateTo(Screen.BookSettings)
                            },
                            viewModel = playerViewModel,
                            onPlayback = {
                            }
                        )
                    }
                }
            }
        }
    }
}