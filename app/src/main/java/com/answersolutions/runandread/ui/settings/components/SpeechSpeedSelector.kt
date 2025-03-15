package com.answersolutions.runandread.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import java.util.Locale

@Composable
fun SpeechSpeedSelector(
    defaultSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    var selectedSpeed by remember { mutableFloatStateOf(defaultSpeed) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Speech Rate",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(speeds) { speed ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
//                        .size(50.dp)
                        .background(
                            if (selectedSpeed == speed) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 2.dp)
                        .clickable {
                            selectedSpeed = speed
                            onSpeedSelected(speed)
                        }
                ) {
                    Text(
                        text = String.format(Locale.getDefault(),"%.2f", speed),
                        color = if (selectedSpeed == speed) MaterialTheme.colorScheme.surface else Color.Black,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpeechSpeedSelectorPreview() {
    RunAndReadTheme(darkTheme = false) {
        SpeechSpeedSelector(defaultSpeed = 1.0f) { newSpeed ->
            println("Selected speed: $newSpeed")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SpeechSpeedSelectorPreviewDark() {
    RunAndReadTheme(darkTheme = true) {
        SpeechSpeedSelector(defaultSpeed = 1.0f) { newSpeed ->
            println("Selected speed: $newSpeed")
        }
    }
}