package com.answersolutions.runandread.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.answersolutions.runandread.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {
    private val _navigationEvents = MutableSharedFlow<NavigationCommand>()
    private val navigationEvents: SharedFlow<NavigationCommand> = _navigationEvents.asSharedFlow()


    fun navigateTo(screen: Screen) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationCommand.Navigate(screen))
        }
    }

    fun popBack() {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationCommand.Back)
        }
    }

    fun onNavigationEvents(navController: NavController) {
        viewModelScope.launch {
            navigationEvents.collect { command ->
                when (command) {
                    is NavigationCommand.Navigate -> navController.navigate(command.screen.route)
                    is NavigationCommand.Back -> navController.popBackStack()
                    is NavigationCommand.NavigateAndReset -> {
//todo
                        navController.navigate(command.screen.route)
                    }
                }
            }
        }
    }
}

sealed class NavigationCommand {
    data class Navigate(val screen: Screen) : NavigationCommand()
    data object Back : NavigationCommand()
    data class NavigateAndReset(val screen: Screen) : NavigationCommand()
}
