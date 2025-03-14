package com.answersolutions.runandread

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
                            navigationViewModel.navigateTo(screen)
                        }, libraryViewModel)
                    }
                    composable(Screen.Home.route) {
                        LibraryScreenView(
                            viewModel = libraryViewModel,
                            onSelect = { book ->
                                libraryViewModel.onSelectBook(book)
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
                            onBookDeleted = {
                                navigationViewModel.resetAndNavigateTo(Screen.Home)
                            },
                            onNavigateBack = { book ->
                                if (book == null) {//open file were canceled
                                    navigationViewModel.popBack()
                                } else {
                                    libraryViewModel.onSelectBook(book)
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

    fun openAppRating(context: Context) {
        val packageName = context.packageName
        try {
            // Open the Play Store app if available
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                ).apply {
                    setPackage("com.android.vending") // Ensure only Play Store handles it
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
        } catch (e: ActivityNotFoundException) {
            // Open in browser if Play Store is unavailable
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
        }
    }

    fun openExternalLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    fun sendEmailToSupport() {
        val text = prepareBugReport()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_EMAIL, "support@answersolutions.net")
            putExtra(Intent.EXTRA_SUBJECT, "RunAndRead Support")
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "RunAndRead Support")
        startActivity(shareIntent)
    }

    private fun prepareBugReport(name: String = "RunAndRead"): String {
        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName: String = BuildConfig.VERSION_NAME
        val model = Build.MODEL
        val version = Build.VERSION.RELEASE


        val messageToSend = StringBuffer(
            """
$name Bug Report
Support Email: support@answersolutions.net
Feedback Email: feedback@answersolutions.net

==Report Begins/Issue/Feedback==========

Please provide here all possible details.
Providing these details can help customer support quickly identify the problem and provide you with the best solution possible.

==Report Ends============

OS Version: $version

Model: $model

App Version: $versionName($versionCode)

For more information, please visit: https://answersolutions.net
"""
        )
        return messageToSend.toString()
    }

}