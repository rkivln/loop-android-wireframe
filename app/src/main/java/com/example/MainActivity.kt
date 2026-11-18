package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifications.NotificationDeepLink
import com.example.ui.LoopApp
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val _pendingDeepLink = MutableStateFlow<NotificationDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure App Check with launch intent before any UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)
        consumeNotificationIntent(intent)

        enableEdgeToEdge()
        setContent {
            val pendingDeepLink by _pendingDeepLink.collectAsStateWithLifecycle()
            LoopApp(
                pendingDeepLink = pendingDeepLink,
                onDeepLinkConsumed = { _pendingDeepLink.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAppCheck(this, intent)
        consumeNotificationIntent(intent)
    }

    private fun consumeNotificationIntent(intent: Intent?) {
        val link = NotificationDeepLink.fromIntent(intent) ?: return
        _pendingDeepLink.value = link
        NotificationDeepLink.clearFromIntent(intent)
    }
}
