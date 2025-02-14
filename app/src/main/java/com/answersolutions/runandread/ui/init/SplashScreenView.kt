package com.answersolutions.runandread.ui.init

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.multidex.BuildConfig
import com.answersolutions.runandread.Screen
import com.answersolutions.runandread.ui.library.LibraryScreenViewModel
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.ui.theme.*
import kotlinx.coroutines.*

@Preview(locale = "iw")
@Composable
fun SplashScreenPreview() {
    RunAndReadTheme(darkTheme = false) {
        SplashScreenContent()
    }
}

@Preview
@Composable
fun SplashScreenDarkPreview() {
    RunAndReadTheme(darkTheme = true) {
        SplashScreenContent()
    }
}

@Composable
fun SplashScreenContent() {
    val versionCode: Int = BuildConfig.VERSION_CODE
    val versionName: String = BuildConfig.VERSION_NAME
    Box(modifier = Modifier
        .background(colorScheme.surface)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(doubleLargeSpace)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Run & Read",
                style = Splash,
                maxLines = 2,
                color = colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(normalSpace))
            Text(
                text = "Your Ultimate Text-to-Speech Player.",
                style = scTypography.headlineMedium,
                maxLines = 2,
                color = colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(normalSpace))
            Text(
                text = "Read with your ears while on the move!",
                style = scTypography.headlineSmall,
                maxLines = 2,
                color = colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(text = "v$versionName($versionCode)", maxLines = 1, color = colorScheme.primary)
            Spacer(modifier = Modifier.height(normalSpace))
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun SplashScreenView(
    onNavigate: (Screen) -> Unit,
    libraryViewModel: LibraryScreenViewModel
) {
    val selectedBook = libraryViewModel.selectedBook.collectAsState()
    LaunchedEffect(selectedBook) {
        val deferred = GlobalScope.async(Dispatchers.IO) {
            Thread.sleep(1000)
        }
        GlobalScope.launch(Dispatchers.Main) {
            deferred.await()
            if (selectedBook.value == null) {
                onNavigate(Screen.Home)
            } else {
                onNavigate(Screen.Player)
            }
        }
    }
    SplashScreenContent()
}