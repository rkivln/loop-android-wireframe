package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBeigeOat
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite
import com.example.update.AppUpdateUiState
import com.example.update.SemanticVersionComparator

@Composable
fun HomeUpdateBanner(
    state: AppUpdateUiState.UpdateAvailable,
    onUpdateNow: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(AccentMint.copy(alpha = 0.55f))
            .border(1.dp, AccentForestGreen.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .padding(start = 14.dp, top = 12.dp, end = 6.dp, bottom = 12.dp)
            .testTag("home_update_banner"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AccentForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = "New release available",
                    tint = InkWhite,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NEW RELEASE · ${state.release.tagName}",
                    style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                    color = AccentForestGreen
                )
                Text(
                    text = state.release.releaseTitle,
                    style = LoopType.TitleMedium.copy(fontSize = 14.5.sp),
                    color = InkCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Installed v${state.installedVersion} · ${state.release.publishedAtLabel}",
                    style = LoopType.Metadata.copy(fontSize = 11.sp),
                    color = InkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("home_update_dismiss_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss update banner",
                    tint = InkSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(AccentForestGreen)
                    .clickable(onClick = onUpdateNow)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .testTag("home_update_now_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Update Now",
                    style = LoopType.Metadata.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = InkWhite
                )
            }
        }
    }
}

@Composable
fun ReleaseNotesDialog(
    state: AppUpdateUiState.UpdateAvailable,
    onOpenGitHubRelease: () -> Unit,
    onSkipVersion: () -> Unit,
    onDismiss: () -> Unit
) {
    val release = state.release
    val assetSizeLabel = release.apkAssetSizeBytes
        ?.let { SemanticVersionComparator.formatByteSize(it) }
        ?.takeIf { it.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperPureWhite,
        titleContentColor = InkCharcoal,
        textContentColor = InkSecondary,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(AccentMint)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "v${state.installedVersion} → ${release.tagName}",
                            style = LoopType.Metadata.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AccentForestGreen
                        )
                    }
                    Text(
                        text = release.publishedAtLabel,
                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                        color = InkMuted
                    )
                }
                Text(
                    text = release.releaseTitle,
                    style = LoopType.HeadlineMedium.copy(fontSize = 19.sp),
                    color = InkCharcoal
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Source: github.com/${state.configuredRepo}",
                    style = LoopType.Metadata.copy(fontSize = 11.5.sp),
                    color = InkSecondary
                )

                if (!release.apkAssetName.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentBeigeOat)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = release.apkAssetName,
                            style = LoopType.Metadata.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = InkCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (assetSizeLabel != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = assetSizeLabel,
                                style = LoopType.Metadata.copy(fontSize = 11.sp),
                                color = InkSecondary
                            )
                        }
                    }
                }

                Text(
                    text = "RELEASE NOTES",
                    style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                    color = InkSecondary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp, max = 220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = release.notes,
                        style = LoopType.BodySmall.copy(lineHeight = 19.sp),
                        color = InkCharcoal,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onSkipVersion,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("skip_update_version_btn")
                ) {
                    Text(
                        text = "Skip Version",
                        color = InkMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("dismiss_release_notes_btn")
                    ) {
                        Text("Later", color = InkCharcoal)
                    }

                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentForestGreen)
                            .clickable(onClick = onOpenGitHubRelease)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("open_github_release_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Open Release",
                                style = LoopType.Metadata.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = InkWhite
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = InkWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun GitHubRepoConfigDialog(
    currentRepo: String,
    defaultBuildConfigRepo: String,
    hasCustomOverride: Boolean,
    onSave: (String) -> Result<String>,
    onResetToDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    var repoInput by remember(currentRepo) { mutableStateOf(currentRepo) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperPureWhite,
        titleContentColor = InkCharcoal,
        textContentColor = InkSecondary,
        title = {
            Text(
                text = "GitHub Release Source",
                style = LoopType.HeadlineMedium.copy(fontSize = 18.sp)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter a public GitHub repository as owner/repo (or paste its GitHub URL) to automatically check for new releases.",
                    style = LoopType.BodySmall,
                    color = InkSecondary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaperIvory)
                        .border(1.dp, PaperBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (repoInput.isEmpty()) {
                        Text(
                            text = "e.g. username/loop-android",
                            style = TextStyle(fontSize = 13.5.sp, color = InkMuted)
                        )
                    }
                    BasicTextField(
                        value = repoInput,
                        onValueChange = {
                            repoInput = it
                            validationError = null
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = InkCharcoal),
                        cursorBrush = SolidColor(InkCharcoal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_repo_input")
                    )
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                        color = AlertCrimson
                    )
                } else if (defaultBuildConfigRepo.isNotBlank()) {
                    Text(
                        text = "Build default: $defaultBuildConfigRepo",
                        style = LoopType.Metadata.copy(fontSize = 11.sp),
                        color = InkMuted
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasCustomOverride) {
                    TextButton(
                        onClick = {
                            onResetToDefault()
                            onDismiss()
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("github_repo_reset_btn")
                    ) {
                        Text(
                            text = if (defaultBuildConfigRepo.isNotBlank()) "Use Default" else "Clear",
                            color = AlertCrimson,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Cancel", color = InkCharcoal)
                    }

                    TextButton(
                        onClick = {
                            onSave(repoInput).fold(
                                onSuccess = { onDismiss() },
                                onFailure = { err ->
                                    validationError = err.localizedMessage
                                        ?: "Use valid owner/repo format."
                                }
                            )
                        },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("github_repo_save_btn")
                    ) {
                        Text(
                            text = "Save & Check",
                            color = AccentForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}
