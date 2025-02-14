package com.answersolutions.runandread.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.answersolutions.runandread.ui.components.NiceButton
import com.answersolutions.runandread.ui.theme.RunAndReadTheme
import com.answersolutions.runandread.ui.theme.*
import java.util.*

@Preview(showBackground = true)
@Composable
fun ConfirmDeleteDialogDarkThemePreview() {
    RunAndReadTheme(darkTheme = true) {
        val test = Locale.getDefault()
        ConfirmDeleteDialog(
            pincode = "",
            buttonEnabled = false,
            onValueChange = {

            },
            onDeleteClicked = {

            },
            onDismissRequest = {

            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteDialogDarkThemePreview() {
    RunAndReadTheme(darkTheme = true) {
        DeleteDialog(
            onDeleteClicked = {

            },
            onDismissRequest = {

            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteDialog(
    pincode: String,
    buttonEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onDeleteClicked: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {onDeleteClicked()},
        dismissButton = {onDismissRequest()},
        title = {
            Text(
                text = "You cannot undo this action!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

        },
        text = @Composable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .height(480.dp)
                    .width(640.dp)
                    .padding(
                        largeSpace
                    )
            ) {
                val focusManager = LocalFocusManager.current
                focusManager.moveFocus(FocusDirection.Enter)
                OutlinedTextField(
                    value = pincode,
                    placeholder = {
                        Text(
                            text = "Input word delete",
                            color = Color.LightGray
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    onValueChange = onValueChange,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(normalSpace))
                Spacer(Modifier.weight(1f))
                Row {
                    NiceButton(
                        enabled = buttonEnabled,
                        title = "DELETE",
                        titleColor = Color.White,
                        color = Color.Red,
                        modifier = Modifier.width(120.dp),
                        clickHandler = onDeleteClicked
                    )
                    Spacer(Modifier.weight(1f))
                    NiceButton(
                        title = "Cancel",
                        color = Color.Gray,
                        titleColor = Color.White,
                        modifier = Modifier.width(120.dp),
                        clickHandler = onDismissRequest
                    )
                }
                Spacer(modifier = Modifier.height(smallSpace))
            }
        }
    )
}

@Composable
fun DeleteDialog(
    onDeleteClicked: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {onDeleteClicked()},
        dismissButton = {onDismissRequest()},
        title = {
            Text(
                text = "Delete Conversation",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = @Composable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .height(200.dp)
                    .padding(
                        largeSpace
                    )
            ) {
                Spacer(modifier = Modifier.height(normalSpace))
                Text(
                    text = "Are you sure? You cannot undo this action!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                Row {
                    NiceButton(
                        title = "DELETE",
                        titleColor = Color.White,
                        color = Color.Red,
                        modifier = Modifier.width(120.dp),
                        clickHandler = onDeleteClicked
                    )
                    Spacer(Modifier.weight(1f))
                    NiceButton(
                        title = "Cancel",
                        color = Color.Gray,
                        titleColor = Color.White,
                        modifier = Modifier.width(120.dp),
                        clickHandler = onDismissRequest
                    )
                }
                Spacer(modifier = Modifier.height(smallSpace))
            }
        }
    )
}
