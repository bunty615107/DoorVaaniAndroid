package com.doorvaani.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doorvaani.ui.theme.DoorVaaniRadius

/**
 * Hexagonal clipped button.
 * Exact clip from active_call_doorvaani code.html:
 * clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
 * + pressed scale + tint.
 *
 * Used for in-call controls grid (Mute/Speaker/Bluetooth/Hold/Record/Keypad etc.)
 * Supports icon + small caps label. Architectural press state (wet stone feel).
 */
private val HexagonShape = GenericShape { size, _ ->
    // Matches the HTML polygon exactly (normalized 0-1)
    moveTo(size.width * 0.5f, 0f)
    lineTo(size.width, size.height * 0.25f)
    lineTo(size.width, size.height * 0.75f)
    lineTo(size.width * 0.5f, size.height)
    lineTo(0f, size.height * 0.75f)
    lineTo(0f, size.height * 0.25f)
    close()
}

@Composable
fun HexButton(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSangam: Boolean = false,
    tint: Color? = null
) {
    val bgColor = if (isSangam) {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.75f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    }

    val contentColor = tint ?: if (isSangam) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.primary
    }

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .size(width = 80.dp, height = 96.dp)  // Matches HTML w-20 h-24 (adjusted for label)
            .clip(HexagonShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom press below via scale on parent if wanted
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(4.dp))
        }

        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}