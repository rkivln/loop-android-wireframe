package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.models.LoopCategory
import com.example.data.repository.LoopRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("loop", appName)
    }

    @Test
    fun `repository operations work as expected`() {
        val repository = LoopRepository()

        // User profile test
        val user = repository.currentUser.value
        assertEquals("Gokulan", user.name)
        assertEquals("@gokulan", user.handle)

        // Post creation test
        val initialPosts = repository.posts.value.size
        repository.addPost("Excited to study graph theory!", LoopCategory.STUDY, 2)
        assertEquals(initialPosts + 1, repository.posts.value.size)
        assertEquals("Excited to study graph theory!", repository.posts.value.first().content)

        // Post like test
        val firstPostId = repository.posts.value.first().id
        assertFalse(repository.posts.value.first().isLiked)
        repository.togglePostLike(firstPostId)
        assertTrue(repository.posts.value.first().isLiked)

        // Chat message test
        val initialMessages = repository.messages.value.size
        repository.sendMessage("See you at 5!")
        assertEquals(initialMessages + 1, repository.messages.value.size)
        assertEquals("See you at 5!", repository.messages.value.last().text)

        // Event attendance toggle
        val event = repository.events.value.first()
        val initialGoing = event.isGoing
        repository.toggleEventAttendance(event.id)
        assertEquals(!initialGoing, repository.events.value.first().isGoing)
    }
}
