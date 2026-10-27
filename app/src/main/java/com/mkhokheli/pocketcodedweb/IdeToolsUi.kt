package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared surfaces and controls for tools embedded in the existing editor. */
internal data class IdeToolColors(
    val surface: Color,
    val panel: Color,
    val codeBackground: Color,
    val selected: Color,
    val text: Color,
    val mutedText: Color,
    val border: Color,
    val accent: Color,
    val onAccent: Color,
    val error: Color,
    val success: Color,
    val warning: Color,
    val run: Color,
    val onRun: Color,
)

internal val LocalIdeToolColors = staticCompositionLocalOf<IdeToolColors?> { null }

@Composable
internal fun ideToolColors(): IdeToolColors {
    LocalIdeToolColors.current?.let { return it }
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.3f
    return IdeToolColors(
        surface = scheme.surface, panel = scheme.surface,
        codeBackground = scheme.background, selected = scheme.primaryContainer,
        text = scheme.onSurface, mutedText = scheme.onSurfaceVariant,
        border = scheme.outline, accent = scheme.primary, onAccent = scheme.onPrimary,
        error = scheme.error,
        success = if (dark) Color(0xFF59E391) else Color(0xFF087A43),
        warning = if (dark) Color(0xFFFFCC66) else Color(0xFF8A5300),
        run = scheme.primary, onRun = scheme.onPrimary,
    )
}

internal val IdeToolDialogShape = RoundedCornerShape(14.dp)
internal val IdeToolControlShape = RoundedCornerShape(8.dp)

@Composable
internal fun IdeToolDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
) {
    val colors = ideToolColors()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("ide-tool-dialog").border(1.dp, colors.border, IdeToolDialogShape),
        shape = IdeToolDialogShape,
        containerColor = colors.surface,
        tonalElevation = 0.dp,
        titleContentColor = colors.text,
        textContentColor = colors.text,
        title = { ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)) { title() } },
        text = { ProvideTextStyle(MaterialTheme.typography.bodyMedium) { text() } },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
    )
}

@Composable
internal fun IdeToolSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = ideToolColors()
    Surface(
        modifier = modifier.testTag("ide-tool-surface"),
        shape = IdeToolDialogShape,
        color = colors.surface, contentColor = colors.text,
        border = BorderStroke(1.dp, colors.border), tonalElevation = 0.dp,
    ) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) { content() }
    }
}

@Composable
internal fun IdeToolPrimaryButton(
    onClick: () -> Unit, enabled: Boolean = true, runAction: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = ideToolColors()
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier, shape = IdeToolControlShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (runAction) colors.run else colors.accent,
            contentColor = if (runAction) colors.onRun else colors.onAccent,
            disabledContainerColor = colors.selected.copy(alpha = 0.45f),
            disabledContentColor = colors.mutedText.copy(alpha = 0.6f),
        ), content = content,
    )
}

@Composable
internal fun IdeToolTextButton(
    onClick: () -> Unit, enabled: Boolean = true, destructive: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = ideToolColors()
    TextButton(
        onClick = onClick, enabled = enabled, shape = IdeToolControlShape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (destructive) colors.error else colors.accent,
            disabledContentColor = colors.mutedText.copy(alpha = 0.6f),
        ), content = content,
    )
}

@Composable
internal fun IdeToolOutlinedButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false,
    enabled: Boolean = true, content: @Composable RowScope.() -> Unit,
) {
    val colors = ideToolColors()
    OutlinedButton(
        onClick = onClick, modifier = modifier.semantics { this.selected = selected }, enabled = enabled, shape = IdeToolControlShape,
        border = BorderStroke(1.dp, if (selected) colors.accent else colors.border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) colors.selected else Color.Transparent,
            contentColor = if (selected) colors.accent else colors.text,
            disabledContentColor = colors.mutedText.copy(alpha = 0.6f),
        ), content = content,
    )
}

@Composable
internal fun IdeToolTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    modifier: Modifier = Modifier.fillMaxWidth(), singleLine: Boolean = false, minLines: Int = 1,
) {
    val colors = ideToolColors()
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier, singleLine = singleLine, minLines = minLines,
        shape = IdeToolControlShape, textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.text, unfocusedTextColor = colors.text,
            focusedContainerColor = colors.panel, unfocusedContainerColor = colors.panel,
            cursorColor = colors.accent, focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border, focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.mutedText, errorTextColor = colors.error,
        ),
    )
}
