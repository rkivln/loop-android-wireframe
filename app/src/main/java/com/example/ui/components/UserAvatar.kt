package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.models.AVATAR_PRESETS
import com.example.ui.theme.LightInkCharcoal
import com.example.ui.theme.LoopType

@Composable
fun UserAvatar(
    avatarUrl: String?,
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    avatarPresetIndex: Int? = null,
    fallbackContainerColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    contentDescription: String? = "Profile avatar"
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cleanUrl = remember(avatarUrl) {
        avatarUrl?.trim()?.takeIf {
            it.startsWith("http://", ignoreCase = true) ||
                it.startsWith("https://", ignoreCase = true) ||
                it.startsWith("content://", ignoreCase = true) ||
                it.startsWith("file://", ignoreCase = true)
        }
    }
    val resolvedFallbackColor = remember(avatarPresetIndex, fallbackContainerColor) {
        when {
            avatarPresetIndex != null -> {
                val preset = AVATAR_PRESETS.getOrElse(avatarPresetIndex.coerceAtLeast(0) % AVATAR_PRESETS.size) {
                    AVATAR_PRESETS[0]
                }
                Color(preset.bgHex1)
            }
            fallbackContainerColor != null -> fallbackContainerColor
            else -> Color(AVATAR_PRESETS[0].bgHex1)
        }
    }
    val requestedPx = remember(size, density) {
        with(density) { size.roundToPx().coerceIn(96, 512) }
    }

    val containerModifier = modifier
        .size(size)
        .clip(CircleShape)
        .let { base ->
            if (borderColor != null && borderWidth > 0.dp) {
                base.border(borderWidth, borderColor, CircleShape)
            } else {
                base
            }
        }
        .let { base ->
            if (contentDescription != null) {
                base.semantics { this.contentDescription = contentDescription }
            } else {
                base
            }
        }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        if (cleanUrl != null) {
            val imageRequest = remember(cleanUrl, requestedPx, context) {
                ImageRequest.Builder(context)
                    .data(cleanUrl)
                    .size(requestedPx)
                    .crossfade(180)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    ShimmerImageBox(modifier = Modifier.fillMaxSize())
                },
                error = {
                    InitialsAvatarFallback(
                        initials = initials,
                        backgroundColor = resolvedFallbackColor,
                        size = size
                    )
                }
            )
        } else {
            InitialsAvatarFallback(
                initials = initials,
                backgroundColor = resolvedFallbackColor,
                size = size
            )
        }
    }
}

@Composable
private fun InitialsAvatarFallback(
    initials: String,
    backgroundColor: Color,
    size: Dp
) {
    val fontSizeSp = remember(size) {
        (size.value * 0.34f).coerceIn(11f, 26f).sp
    }
    val textColor = remember(backgroundColor) {
        if (backgroundColor.luminance() > 0.5f) LightInkCharcoal else Color.White
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifBlank { "U" },
            style = LoopType.HeadlineMedium.copy(
                fontSize = fontSizeSp,
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor,
            maxLines = 1
        )
    }
}
