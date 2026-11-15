package com.example.ui.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.theme.AccentForestGreen
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentPeach
import com.example.ui.theme.AccentPowderBlue
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.InkWhite
import com.example.ui.theme.LoopType
import com.example.ui.theme.PaperBorderSubtle
import com.example.ui.theme.PaperIvory
import com.example.ui.theme.PaperPureWhite

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Attempt silent auto-sign-in on initial composition
    LaunchedEffect(Unit) {
        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = { /* stay on AuthScreen */ },
            scope = coroutineScope
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PaperIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Editorial Masthead
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(PaperPureWhite)
                        .border(1.dp, PaperBorderSubtle, RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "MEMBER ACCESS · GOOGLE VERIFIED",
                        style = LoopType.EditorialTag.copy(fontSize = 10.sp),
                        color = InkSecondary
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "LOOP",
                    style = LoopType.HeroDisplayLarge.copy(
                        fontSize = 40.sp,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = InkCharcoal
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "The Living Community Catalog",
                    style = LoopType.EditorialTag,
                    color = AccentForestGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sign in with Google to publish items, share local posts, and reserve gear from neighbors near you.",
                    style = LoopType.BodyEditorial,
                    color = InkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }

            // Editorial Feature Summary Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(PaperPureWhite)
                    .border(1.dp, PaperBorderSubtle, RoundedCornerShape(22.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EditorialFeatureRow(
                    icon = Icons.Default.CameraAlt,
                    badgeColor = AccentPowderBlue,
                    title = "Community Marketplace",
                    subtitle = "Cameras, tools, electronics, furniture & mobility"
                )
                EditorialFeatureRow(
                    icon = Icons.Default.Groups,
                    badgeColor = AccentPeach,
                    title = "Local Events & Meetups",
                    subtitle = "Neighborhood meetups, skill shares & study circles"
                )
                EditorialFeatureRow(
                    icon = Icons.Default.CheckCircle,
                    badgeColor = AccentMint,
                    title = "Real-time Studio Dispatch",
                    subtitle = "Instant host chat, bookings & Gemini AI pricing"
                )
            }

            // Google Sign-In CTA
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = AlertCrimson,
                        style = LoopType.BodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = {
                        val activity = context as? Activity ?: return@Button
                        isLoading = true
                        errorMessage = null
                        AuthManager.onGoogleSignInClicked(
                            activity = activity,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { errorMsg ->
                                isLoading = false
                                errorMessage = errorMsg
                            },
                            scope = coroutineScope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkCharcoal,
                        contentColor = InkWhite
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = InkWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                style = LoopType.ButtonLabel
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Verified access with Google & Firebase",
                    style = LoopType.Metadata,
                    color = InkMuted
                )
            }
        }
    }
}

@Composable
private fun EditorialFeatureRow(
    icon: ImageVector,
    badgeColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InkCharcoal,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = LoopType.TitleMedium.copy(fontSize = 14.5.sp),
                color = InkCharcoal
            )
            Text(
                text = subtitle,
                style = LoopType.BodySmall,
                color = InkSecondary
            )
        }
    }
}
