package com.ghazarian.wordclock.feature.devices.presentation

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.ghazarian.wordclock.core.designsystem.theme.ThemeMode
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.app_version
import com.ghazarian.wordclock.resources.appearance
import com.ghazarian.wordclock.resources.contact_brand
import com.ghazarian.wordclock.resources.contact_call_hint
import com.ghazarian.wordclock.resources.contact_description
import com.ghazarian.wordclock.resources.contact_instagram_handle
import com.ghazarian.wordclock.resources.contact_instagram_hint
import com.ghazarian.wordclock.resources.contact_instagram_label
import com.ghazarian.wordclock.resources.contact_phone_label
import com.ghazarian.wordclock.resources.contact_phone_number
import com.ghazarian.wordclock.resources.contact_title
import com.ghazarian.wordclock.resources.contact_us
import com.ghazarian.wordclock.resources.dark_theme
import com.ghazarian.wordclock.resources.language
import com.ghazarian.wordclock.resources.language_english
import com.ghazarian.wordclock.resources.language_persian
import com.ghazarian.wordclock.resources.light_theme
import com.ghazarian.wordclock.resources.settings_description
import com.ghazarian.wordclock.resources.settings_title
import com.ghazarian.wordclock.core.designsystem.R as DesignSystemR

@Composable
internal fun SettingsDrawer(
    themeMode: ThemeMode,
    languageCode: String,
    appVersion: String,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onContactUs: () -> Unit,
) {
    var languageOptionsExpanded by remember { mutableStateOf(false) }
    val systemIsDark = isSystemInDarkTheme()
    val currentlyDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemIsDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val currentThemeLabel = stringResource(
        if (currentlyDark) MR.strings.dark_theme.resourceId else MR.strings.light_theme.resourceId,
    )

    BoxWithConstraints {
        Surface(
            modifier = Modifier
                .width(maxWidth * 0.8f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding() + 24.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 16.dp,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Image(
                            painter = painterResource(DesignSystemR.drawable.word_clock_logo),
                            contentDescription = null,
                            modifier = Modifier.padding(7.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = stringResource(MR.strings.settings_title.resourceId),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(MR.strings.settings_description.resourceId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                )
                SettingsActionCard {
                    SettingsActionRow(
                        iconResource = if (currentlyDark) {
                            DesignSystemR.drawable.sunny_24
                        } else {
                            DesignSystemR.drawable.dark_mode_24
                        },
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        title = stringResource(MR.strings.appearance.resourceId),
                        detail = currentThemeLabel,
                        onClick = {
                            onThemeModeChanged(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
                        },
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                SettingsActionCard {
                    SettingsActionRow(
                        iconResource = DesignSystemR.drawable.language_24,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        title = stringResource(MR.strings.language.resourceId),
                        detail = stringResource(
                            if (languageCode == "fa") {
                                MR.strings.language_persian.resourceId
                            } else {
                                MR.strings.language_english.resourceId
                            },
                        ),
                        expanded = languageOptionsExpanded,
                        onClick = { languageOptionsExpanded = !languageOptionsExpanded },
                    )
                    if (languageOptionsExpanded) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LanguageOption(
                                label = stringResource(MR.strings.language_english.resourceId),
                                selected = languageCode == "en",
                                onClick = { onLanguageChanged("en") },
                                modifier = Modifier.weight(1f),
                            )
                            LanguageOption(
                                label = stringResource(MR.strings.language_persian.resourceId),
                                selected = languageCode == "fa",
                                onClick = { onLanguageChanged("fa") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                SettingsActionCard {
                    SettingsActionRow(
                        iconResource = DesignSystemR.drawable.call_24,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        title = stringResource(MR.strings.contact_us.resourceId),
                        detail = stringResource(MR.strings.contact_brand.resourceId),
                        onClick = onContactUs,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                )
                Text(
                    text = stringResource(MR.strings.app_version.resourceId, appVersion),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SettingsActionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsActionRow(
    iconResource: Int,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    detail: String,
    expanded: Boolean? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(15.dp),
            color = iconContainerColor,
        ) {
            Icon(
                painter = painterResource(iconResource),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(11.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(
                if (expanded == true) DesignSystemR.drawable.expand_more_24
                else DesignSystemR.drawable.chevron_right_24,
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun ContactUsSheetContent() {
    val context = LocalContext.current
    val phoneNumber = stringResource(MR.strings.contact_phone_number.resourceId)
    val instagramHandle = stringResource(MR.strings.contact_instagram_handle.resourceId)
    val instagramUrl = "https://www.instagram.com/${instagramHandle.removePrefix("@").trim()}/"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .heightIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Image(
                    painter = painterResource(DesignSystemR.drawable.word_clock_logo),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = stringResource(MR.strings.contact_brand.resourceId),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(MR.strings.contact_title.resourceId),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Text(
            text = stringResource(MR.strings.contact_description.resourceId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ContactActionCard(
            icon = DesignSystemR.drawable.call_24,
            label = stringResource(MR.strings.contact_phone_label.resourceId),
            detail = phoneNumber,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri()))
            },
        )
        ContactActionCard(
            icon = DesignSystemR.drawable.photo_camera_24,
            label = stringResource(MR.strings.contact_instagram_label.resourceId),
            detail = instagramHandle,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, instagramUrl.toUri()))
            },
        )
    }
}

@Composable
private fun ContactActionCard(
    icon: Int,
    label: String,
    detail: String,
    onClick: () -> Unit,
) {
    val description = when (icon) {
        DesignSystemR.drawable.call_24 -> stringResource(MR.strings.contact_call_hint.resourceId)
        else -> stringResource(MR.strings.contact_instagram_hint.resourceId)
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = "$label. $detail. $description"
                },
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    painter = painterResource(DesignSystemR.drawable.open_in_new_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
