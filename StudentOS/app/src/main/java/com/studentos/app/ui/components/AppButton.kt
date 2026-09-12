package com.studentos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.studentos.app.ui.theme.AppRadius
import com.studentos.app.ui.theme.Spacing

enum class ButtonStyle {
    Primary, Secondary, Ghost, Danger
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    fullWidth: Boolean = false,
    height: Dp = 48.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "btn_scale")

    val containerColor = when (style) {
        ButtonStyle.Primary -> MaterialTheme.colorScheme.primary
        ButtonStyle.Secondary -> MaterialTheme.colorScheme.secondaryContainer
        ButtonStyle.Ghost -> Color.Transparent
        ButtonStyle.Danger -> MaterialTheme.colorScheme.error
    }
    val contentColor = when (style) {
        ButtonStyle.Primary -> Color.White
        ButtonStyle.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
        ButtonStyle.Ghost -> MaterialTheme.colorScheme.primary
        ButtonStyle.Danger -> Color.White
    }

    when (style) {
        ButtonStyle.Ghost -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.scale(scale).then(if (fullWidth) Modifier.fillMaxWidth() else Modifier).height(height),
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = Spacing.base, vertical = Spacing.sm)
            ) {
                ButtonContent(text, icon, loading, contentColor)
            }
        }
        ButtonStyle.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.scale(scale).then(if (fullWidth) Modifier.fillMaxWidth() else Modifier).height(height),
                enabled = enabled && !loading,
                shape = RoundedCornerShape(AppRadius.md),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = Spacing.base, vertical = Spacing.sm)
            ) {
                ButtonContent(text, icon, loading, contentColor)
            }
        }
        else -> {
            Button(
                onClick = onClick,
                modifier = modifier.scale(scale).then(if (fullWidth) Modifier.fillMaxWidth() else Modifier).height(height),
                enabled = enabled && !loading,
                shape = RoundedCornerShape(AppRadius.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = Spacing.base, vertical = Spacing.sm)
            ) {
                ButtonContent(text, icon, loading, contentColor)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    loading: Boolean,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Extension for fillMaxWidth
private fun Modifier.fillMaxWidth(): Modifier = this.then(androidx.compose.foundation.layout.fillMaxWidth())
