package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.models.CommunityPost
import com.example.data.models.EventItem
import com.example.data.models.LoopCategory
import com.example.data.repository.LoopRepository
import com.example.ui.components.LoopBottomNavigationBar
import com.example.ui.components.LoopNavScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.EventDetailsScreen
import com.example.ui.screens.ExploreMapScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Welcome : ScreenDestination()
    object Onboarding : ScreenDestination()
    object Main : ScreenDestination()
    object CreatePost : ScreenDestination()
    data class EventDetails(val eventId: String) : ScreenDestination()
    object Menu : ScreenDestination()
}

@Composable
fun LoopApp(
    repository: LoopRepository = remember { LoopRepository() }
) {
    var currentDestination by remember { mutableStateOf<ScreenDestination>(ScreenDestination.Welcome) }
    var currentNavTab by remember { mutableStateOf(LoopNavScreen.HOME) }

    val currentUser by repository.currentUser.collectAsState()
    val posts by repository.posts.collectAsState()
    val events by repository.events.collectAsState()
    val mapPins by repository.mapPins.collectAsState()
    val selectedMapPin by repository.selectedMapPin.collectAsState()
    val messages by repository.messages.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (currentDestination is ScreenDestination.Main) {
                        LoopBottomNavigationBar(
                            currentScreen = currentNavTab,
                            onNavigate = { tab ->
                                currentNavTab = tab
                            },
                            onPlusClick = {
                                currentDestination = ScreenDestination.CreatePost
                            }
                        )
                    }
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "screen_transition",
                    modifier = Modifier.padding(
                        bottom = if (currentDestination is ScreenDestination.Main) 0.dp else innerPadding.calculateBottomPadding()
                    )
                ) { destination ->
                    when (destination) {
                        is ScreenDestination.Welcome -> {
                            WelcomeScreen(
                                onContinue = {
                                    currentDestination = ScreenDestination.Onboarding
                                }
                            )
                        }

                        is ScreenDestination.Onboarding -> {
                            OnboardingScreen(
                                onFinish = {
                                    currentDestination = ScreenDestination.Main
                                    currentNavTab = LoopNavScreen.HOME
                                }
                            )
                        }

                        is ScreenDestination.Main -> {
                            when (currentNavTab) {
                                LoopNavScreen.HOME -> {
                                    HomeScreen(
                                        onNavigateExplore = {
                                            currentNavTab = LoopNavScreen.EXPLORE
                                        },
                                        onNavigateEvents = {
                                            currentDestination = ScreenDestination.EventDetails(events.first().id)
                                        },
                                        onNavigateStudy = {
                                            currentNavTab = LoopNavScreen.EXPLORE
                                        },
                                        onNavigateHelp = {
                                            currentNavTab = LoopNavScreen.EXPLORE
                                        },
                                        onNavigatePeople = {
                                            currentNavTab = LoopNavScreen.EXPLORE
                                        },
                                        onOpenNotifications = {
                                            currentDestination = ScreenDestination.Menu
                                        }
                                    )
                                }

                                LoopNavScreen.EXPLORE -> {
                                    ExploreMapScreen(
                                        pins = mapPins,
                                        selectedPin = selectedMapPin,
                                        onPinSelect = { pin -> repository.selectMapPin(pin) },
                                        onNavigateEventDetails = { eventId ->
                                            currentDestination = ScreenDestination.EventDetails(eventId)
                                        },
                                        onBack = {
                                            currentNavTab = LoopNavScreen.HOME
                                        }
                                    )
                                }

                                LoopNavScreen.COMMUNITY -> {
                                    CommunityScreen(
                                        posts = posts,
                                        onToggleLike = { postId -> repository.togglePostLike(postId) },
                                        onToggleSave = { postId -> repository.togglePostSaved(postId) },
                                        onOpenMenu = { currentDestination = ScreenDestination.Menu },
                                        onOpenPostDetails = { /* post details */ }
                                    )
                                }

                                LoopNavScreen.CHATS -> {
                                    ChatScreen(
                                        messages = messages,
                                        onSendMessage = { text -> repository.sendMessage(text) },
                                        onToggleAudio = { id -> repository.toggleAudioPlayback(id) },
                                        onBack = { currentNavTab = LoopNavScreen.HOME }
                                    )
                                }

                                LoopNavScreen.PROFILE -> {
                                    ProfileScreen(
                                        user = currentUser,
                                        onOpenMenu = { currentDestination = ScreenDestination.Menu },
                                        onOpenEvent = {
                                            currentDestination = ScreenDestination.EventDetails(events.first().id)
                                        }
                                    )
                                }
                            }
                        }

                        is ScreenDestination.EventDetails -> {
                            val event = events.find { it.id == destination.eventId } ?: events.first()
                            EventDetailsScreen(
                                event = event,
                                onToggleGoing = { eventId ->
                                    repository.toggleEventAttendance(eventId)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (!event.isGoing) "You're attending ${event.title}!" else "Removed from attendance"
                                        )
                                    }
                                },
                                onBack = {
                                    currentDestination = ScreenDestination.Main
                                }
                            )
                        }

                        is ScreenDestination.CreatePost -> {
                            CreatePostScreen(
                                onPostCreated = { content, category, visualType ->
                                    repository.addPost(content, category, visualType)
                                    currentDestination = ScreenDestination.Main
                                    currentNavTab = LoopNavScreen.COMMUNITY
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Post published to nearby community!")
                                    }
                                },
                                onClose = {
                                    currentDestination = ScreenDestination.Main
                                }
                            )
                        }

                        is ScreenDestination.Menu -> {
                            MenuScreen(
                                onNavigateProfile = {
                                    currentDestination = ScreenDestination.Main
                                    currentNavTab = LoopNavScreen.PROFILE
                                },
                                onBack = {
                                    currentDestination = ScreenDestination.Main
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
