package com.answersolutions.runandread.ui.player

import android.text.TextUtils
import android.view.View.LAYOUT_DIRECTION_RTL
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import java.util.Locale


@Preview(showBackground = true)
@Composable
fun HorizontallyScrolledTextViewPreviewDark() {
    RunAndReadTheme(darkTheme = true) {
        HorizontallyScrolledTextView(
            highLight=true,
            words = listOf("Hello", "Compose", "Preview", "How", "Are", "You", "Doing", "Today"),
            index = 1,
            Locale.getDefault()
        )
    }

}

@Preview(showBackground = true)
@Composable
fun HorizontallyScrolledTextViewPreview() {
    RunAndReadTheme(darkTheme = false) {
        HorizontallyScrolledTextView(
            highLight=false,
            words = listOf("Hello", "Compose", "Preview", "How", "Are", "You", "Doing", "Today"),
            index = 1,
            Locale.getDefault()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontallyScrolledTextViewPreviewRTL() {
    RunAndReadTheme(darkTheme = false) {
        HorizontallyScrolledTextView(
            highLight=true,
            words = listOf("שלום", "כתוב", "תצוגה מקדימה", "איך", "אתה", "עושה", "היום"),
            index = 4,
            Locale("iw", "IL")
        )
    }
}

@Composable
fun HorizontallyScrolledTextView(
    highLight: Boolean = true,
    words: List<String>, index: Int, language: Locale) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isRTL = TextUtils.getLayoutDirectionFromLocale(language) == LAYOUT_DIRECTION_RTL
    val layoutDirection = if (isRTL) LayoutDirection.Rtl else LayoutDirection.Ltr

    LaunchedEffect(index) {
        coroutineScope.launch {
            listState.animateScrollToItem(
                (index - 3).coerceAtLeast(0),
                scrollOffset = (index + 3).coerceAtMost(words.size - 1)
            )
        }
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier.background(colorScheme.background)
            .fillMaxWidth()
            .height(50.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(words) { i, word ->
                    val isSelected = (i == index - 1)
                    val backgroundColor = if (isSelected && highLight) colorScheme.primary else Color.Transparent
                    val textColor = if (isSelected && highLight) colorScheme.surface else colorScheme.onSurface

                    Box(
                        modifier = Modifier
                            .background(backgroundColor)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        BasicText(
                            text = word,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = if (isSelected&& highLight) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}