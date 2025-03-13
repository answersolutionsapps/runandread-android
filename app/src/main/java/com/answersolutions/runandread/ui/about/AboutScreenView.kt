package com.answersolutions.runandread.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.answersolutions.runandread.BuildConfig
import com.answersolutions.runandread.MainActivity
import com.answersolutions.runandread.ui.components.NiceButtonLarge
import com.answersolutions.runandread.ui.theme.RunAndReadTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
)  {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("About Run & Read") },
                actions = {
                    Text("Library",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                onNavigateBack()
                            })
                    Spacer(modifier = Modifier.weight(1F))
                })
        }
    ) { padding ->
        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName: String = BuildConfig.VERSION_NAME
        val context = LocalContext.current as MainActivity
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Description
            Text("Run & Read is a free, user-friendly text-to-speech app designed to bring your digital content to life. Using Android's text-to-speech engine, our app converts PDFs, EPUBs, TXT files, or any copied text into engaging audio—so you can enjoy your favorite books and articles anytime, anywhere.")

            // Our Mission Section
            Text("Our Mission", style = MaterialTheme.typography.headlineSmall)
            Text("We believe that great literature and valuable information should be accessible to everyone. Run & Read makes it easy to listen to your digital content while you’re on the go—whether you’re exercising, commuting, or simply relaxing.")

            // Curated Library Section
            Text("Curated Public Domain Library", style = MaterialTheme.typography.headlineSmall)
            Text("To help you get started, we’ve preloaded a selection of classic books from public domain sources, including titles from Project Gutenberg. These timeless works are legally free to use and share, allowing you to explore classic literature without any copyright concerns. We encourage you to support authors and publishers by enjoying content that you have legally acquired.")

            // Link to Project Gutenberg
            Text("Visit Project Gutenberg website:")
            TextButton(onClick = {
                context.openExternalLink("https://gutenberg.org")
            }) {
                Text(
                    "www.gutenberg.org",
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }

            // Legal & Copyright Notice Section
            Text("Legal & Copyright Notice", style = MaterialTheme.typography.headlineSmall)
            Text("Run & Read is committed to respecting intellectual property rights. Please use this app only for your personally purchased digital content or for works that are in the public domain. For preloaded books from Project Gutenberg and other sources, we adhere strictly to their guidelines and terms of use.")

            Text("Thank you for choosing Run & Read. We hope our app enriches your daily routine by making reading more accessible and enjoyable!")

            // Website Link
            Text("Visit our website:")
            TextButton(onClick = {
                context.openExternalLink("https://answersolutions.net")
            }) {
                Text(
                    "www.answersolutions.net",
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }

            Text("Open-source tooling to create audiobooks in the Run & Read format:")
            TextButton(onClick = {
                context.openExternalLink("https://github.com/sergenes/runandread-audiobook")
            }) {
                Text(
                    "runandread-audiobook on GitHub",
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }

            // App Version
            Text("App version:$versionName($versionCode)", style = MaterialTheme.typography.bodySmall)

            // Report an Issue Button
            NiceButtonLarge(title = "Report an Issue", color = colorScheme.primary) {

                context.sendEmailToSupport()
            }
            // Divider
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            NiceButtonLarge(title = "Rate the App", color = colorScheme.primary) {
                context.openAppRating(context)
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    RunAndReadTheme(darkTheme = true) {
        AboutScreen(onNavigateBack = {})
    }
}