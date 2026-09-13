package com.studentos.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studentos.app.ui.theme.AppRadius
import com.studentos.app.ui.theme.Spacing

@Composable
fun <T> FilterChipGroup(
    items: List<T>,
    selectedItem: T?,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
    scrollable: Boolean = true
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier.then(
            if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier
        ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items.forEach { item ->
            val selected = item == selectedItem
            val containerColor by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                label = "chip_color"
            )
            val contentColor by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "chip_content"
            )

            FilterChip(
                selected = selected,
                onClick = { onSelected(item) },
                label = {
                    Text(
                        text = label(item),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = RoundedCornerShape(AppRadius.full),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
        // Add end padding for scroll
        if (scrollable) {
            Spacer(modifier = Modifier.width(Spacing.xs))
        }
    }
}

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(Spacing.xs)
            .padding(horizontal = Spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            val bgColor by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                label = "seg_color"
            )
            val textColor by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "seg_text"
            )

            androidx.compose.material3.Surface(
                onClick = { onSelected(index) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(AppRadius.sm),
                color = bgColor,
                border = if (!selected) BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant) else null
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    modifier = Modifier.padding(vertical = Spacing.sm),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
