package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RentalRepository
import org.junit.Assert.assertEquals
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
    fun `rental repository profile initializes with Jaya Nandhini G`() {
        val repository = RentalRepository
        val user = repository.userProfileFlow.value
        assertEquals("Jaya Nandhini G", user.displayName)
        assertEquals("JN", user.initials)
    }
}

