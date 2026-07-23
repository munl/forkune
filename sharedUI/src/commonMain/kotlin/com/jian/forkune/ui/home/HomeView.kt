package com.jian.forkune.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.jian.forkune.theme.Variables
import forkune.sharedui.generated.resources.Res
import forkune.sharedui.generated.resources.home_days_ago
import forkune.sharedui.generated.resources.home_choose_location
import forkune.sharedui.generated.resources.home_filters
import forkune.sharedui.generated.resources.home_headline
import forkune.sharedui.generated.resources.home_last_pick
import forkune.sharedui.generated.resources.home_location_content_description
import forkune.sharedui.generated.resources.home_pick_cuisines
import forkune.sharedui.generated.resources.home_profile_content_description
import forkune.sharedui.generated.resources.home_subtitle
import forkune.sharedui.generated.resources.home_surprise_caption
import forkune.sharedui.generated.resources.home_surprise_content_description
import forkune.sharedui.generated.resources.home_surprise_title
import forkune.sharedui.generated.resources.ic_caret_down
import forkune.sharedui.generated.resources.ic_expand
import forkune.sharedui.generated.resources.ic_location
import forkune.sharedui.generated.resources.ic_person
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

typealias onSurpriseClicked = () -> Unit
typealias onPickCuisinesClicked = () -> Unit
typealias onFiltersClicked = () -> Unit
typealias onProfileClicked = () -> Unit
typealias onLocationClicked = () -> Unit

/**
 * Stateless Home UI (clean-architecture §4). Holds no ViewModel and no business logic —
 * all data and callbacks are parameters. Matches the mock: location chip + profile row,
 * headline + subtitle, the prominent coral Surprise-me card, two secondary buttons and a
 * last-pick footer.
 */
@Composable
fun HomeView(
    location: String?,
    placeCount: Int,
    lastPick: LastPick?,
    onSurpriseClicked: onSurpriseClicked,
    onPickCuisinesClicked: onPickCuisinesClicked,
    onFiltersClicked: onFiltersClicked,
    onProfileClicked: onProfileClicked,
    onLocationClicked: onLocationClicked = {},
    modifier: Modifier = Modifier,
) {
    val dimensions = Variables.Dimensions

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.screenPadding),
    ) {
        TopRow(
            location = location,
            onLocationClicked = onLocationClicked,
            onProfileClicked = onProfileClicked,
        )

        Spacer(Modifier.height(dimensions.spacingXl))

        Text(
            text = stringResource(Res.string.home_headline),
            style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(dimensions.spacingMd))

        Text(
            text = stringResource(Res.string.home_subtitle, placeCount),
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.weight(1f))

        SurpriseCard(onSurpriseClicked = onSurpriseClicked)

        Spacer(Modifier.height(dimensions.spacingMd))

        Row(horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMd)) {
            SecondaryButton(
                label = stringResource(Res.string.home_pick_cuisines),
                onClick = onPickCuisinesClicked,
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(
                label = stringResource(Res.string.home_filters),
                onClick = onFiltersClicked,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(dimensions.spacingLg))

        LastPickFooter(
            lastPick = lastPick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensions.spacingLg),
        )
    }
}

@Composable
private fun TopRow(
    location: String?,
    onLocationClicked: onLocationClicked,
    onProfileClicked: onProfileClicked,
) {
    val dimensions = Variables.Dimensions
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensions.spacingMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            shape = CircleShape,
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer,
            onClick = onLocationClicked,
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = dimensions.spacingMd,
                    vertical = dimensions.spacingSm,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSm),
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_location),
                    contentDescription = stringResource(Res.string.home_location_content_description),
                    tint = Variables.Colors.accent,
                    modifier = Modifier.size(dimensions.spacingLg),
                )
                Text(
                    text = location ?: stringResource(Res.string.home_choose_location),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_caret_down),
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(dimensions.spacingLg),
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer,
            onClick = onProfileClicked,
            modifier = Modifier.size(dimensions.profileButtonSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_person),
                    contentDescription = stringResource(Res.string.home_profile_content_description),
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(dimensions.spacingLg),
                )
            }
        }
    }
}

@Composable
private fun SurpriseCard(onSurpriseClicked: onSurpriseClicked) {
    val dimensions = Variables.Dimensions
    Surface(
        shape = RoundedCornerShape(dimensions.cornerRadius),
        color = Variables.Colors.accent,
        onClick = onSurpriseClicked,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.surpriseCardHeight),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(dimensions.spacingLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.profileButtonSize + dimensions.spacingMd)
                    .clip(CircleShape)
                    .background(Variables.Colors.onAccent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_expand),
                    contentDescription = stringResource(Res.string.home_surprise_content_description),
                    tint = Variables.Colors.onAccent,
                    modifier = Modifier.size(dimensions.spacingXl),
                )
            }
            Spacer(Modifier.height(dimensions.spacingMd))
            Text(
                text = stringResource(Res.string.home_surprise_title),
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Variables.Colors.onAccent,
            )
            Spacer(Modifier.height(dimensions.spacingXs))
            Text(
                text = stringResource(Res.string.home_surprise_caption),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Variables.Colors.onAccent.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = Variables.Dimensions
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(dimensions.buttonCornerRadius),
        modifier = modifier.height(dimensions.secondaryButtonHeight),
    ) {
        Text(
            text = label,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LastPickFooter(
    lastPick: LastPick?,
    modifier: Modifier = Modifier,
) {
    if (lastPick == null) return
    val dimensions = Variables.Dimensions
    val agoText = pluralStringResource(
        Res.plurals.home_days_ago,
        lastPick.daysAgo.toInt(),
        lastPick.daysAgo.toInt(),
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(dimensions.locationDotSize)
                .clip(CircleShape)
                .background(Variables.Colors.success),
        )
        Spacer(Modifier.size(dimensions.spacingSm))
        Text(
            text = stringResource(Res.string.home_last_pick, lastPick.name, agoText),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
