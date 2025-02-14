package com.answersolutions.runandread.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

enum class LoadingState {
    Show,
    Hide
}

class LoadingStateManager {
    lateinit var loading: MutableState<LoadingState>

    @Composable
    fun Remember() {
        loading = remember {
            mutableStateOf(LoadingState.Hide)
        }
    }

    fun onLoadingBegins() {
        loading.value = LoadingState.Show
    }

    fun onLoadingEnds() {
        loading.value = LoadingState.Hide
    }

    fun state() = loading.value
}

data class ErrorState(
    private val errorMessage: String,
    private val errorTitle: String
) {
    fun shouldShow(): Boolean {
        return errorMessage.isNotEmpty()
    }

    fun message(): String {
        return errorMessage
    }

    fun title(): String {
        return errorTitle
    }
}

class ErrorStateManager {
    lateinit var state: MutableState<ErrorState>

    @Composable
    fun Remember() {
        state = remember {
            mutableStateOf(
                ErrorState(
                    errorMessage = "",
                    errorTitle = "Error"
                )
            )
        }
    }

    fun onShowError(title: String? = null, message: String) {
        state.value = state.value.copy(
            errorMessage = message,
            errorTitle = title ?: state.value.title()
        )
    }

    fun onHideError() {
        state.value = ErrorState(
            errorMessage = "",
            errorTitle = "Error"
        )
    }

    fun state(): ErrorState {
        return state.value
    }
}