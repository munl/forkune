package com.jian.forkune.ui.placeholder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jian.forkune.theme.Variables
import forkune.sharedui.generated.resources.Res
import forkune.sharedui.generated.resources.placeholder_coming_soon
import org.jetbrains.compose.resources.stringResource

/**
 * Temporary destination for screens not yet built (Pick cuisines #19, Filters, Shuffling
 * #29). Later epics replace these entries with the real screens.
 */
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(Variables.Dimensions.screenPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.placeholder_coming_soon, title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
