package com.mkhokheli.pocketcodedweb

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import com.mkhokheli.pocketcodedweb.ui.theme.readableContentColor
import com.mkhokheli.pocketcodedweb.ui.theme.readableTextColor

@Composable
internal fun readableFieldColors(): TextFieldColors {
    val scheme = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = scheme.onSurface, unfocusedTextColor = scheme.onSurface,
        disabledTextColor = scheme.onSurfaceVariant, errorTextColor = scheme.onSurface,
        focusedContainerColor = scheme.surface, unfocusedContainerColor = scheme.surface,
        disabledContainerColor = scheme.surface, errorContainerColor = scheme.surface,
        focusedLabelColor = scheme.primary, unfocusedLabelColor = scheme.onSurfaceVariant,
        disabledLabelColor = scheme.onSurfaceVariant, errorLabelColor = scheme.error,
        focusedPlaceholderColor = scheme.onSurfaceVariant, unfocusedPlaceholderColor = scheme.onSurfaceVariant,
        disabledPlaceholderColor = scheme.onSurfaceVariant, errorPlaceholderColor = scheme.onSurfaceVariant,
        focusedSupportingTextColor = scheme.onSurfaceVariant, unfocusedSupportingTextColor = scheme.onSurfaceVariant,
        disabledSupportingTextColor = scheme.onSurfaceVariant, errorSupportingTextColor = scheme.error,
        focusedLeadingIconColor = scheme.onSurfaceVariant, unfocusedLeadingIconColor = scheme.onSurfaceVariant,
        disabledLeadingIconColor = scheme.onSurfaceVariant,
        focusedTrailingIconColor = scheme.onSurfaceVariant, unfocusedTrailingIconColor = scheme.onSurfaceVariant,
        disabledTrailingIconColor = scheme.onSurfaceVariant,
        cursorColor = scheme.primary, errorCursorColor = scheme.error,
        focusedBorderColor = scheme.primary, unfocusedBorderColor = scheme.outline,
        disabledBorderColor = scheme.outline, errorBorderColor = scheme.error,
    )
}

@Composable
internal fun readableButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
internal fun readableOutlinedButtonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors(
    disabledContainerColor = Color.Transparent,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
internal fun readableTextButtonColors(): ButtonColors = ButtonDefaults.textButtonColors(
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
internal fun readableMenuItemColors(): MenuItemColors = MenuDefaults.itemColors(
    textColor = MaterialTheme.colorScheme.onSurface,
    leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

internal fun WebPalette.withReadableContrast(): WebPalette {
    val surfaces = listOf(backgroundTop, backgroundBottom, panel, elevatedPanel, tab, selectedTab, editorBackground, terminalBackground) +
        listOf(accent, error, warning, success, info).map { it.copy(alpha = 0.24f).compositeOver(panel) }
    fun ink(color: Color) = readableTextColor(color, surfaces)
    return copy(text = ink(text), mutedText = ink(mutedText), subtleText = ink(subtleText),
        accent = ink(accent), error = ink(error), warning = ink(warning), success = ink(success), info = ink(info),
        terminalPrompt = ink(terminalPrompt), runText = readableContentColor(run))
}

internal val WebPalette.onAccent: Color get() = readableContentColor(accent)
