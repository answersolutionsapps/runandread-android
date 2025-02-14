package com.answersolutions.extensions

import androidx.compose.ui.Modifier

/**
 * Convenience for modifiers that should only apply if [condition] is true.
 * [elseFn] useful for conditional else-logic.
 */
inline fun Modifier.thenIf(
    condition: Boolean,
    modifierFn: Modifier.() -> Modifier,
    elseFn: Modifier.() -> Modifier,
) = this.let {
    if (condition) {
        it.modifierFn()
    } else {
        it.elseFn()
    }
}

/**
 * Convenience for modifiers that should only apply if [condition] is true.
 * [elseFn] useful for conditional else-logic.
 */
inline fun Modifier.thenIf(
    condition: Boolean,
    modifierFn: Modifier.() -> Modifier,
) = this.let {
    if (condition) {
        it.modifierFn()
    } else {
        it
    }
}