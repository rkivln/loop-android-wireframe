package com.example.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticVersionComparatorTest {

    @Test
    fun `isRemoteNewer handles prefixes and missing trailing zeros`() {
        assertFalse(SemanticVersionComparator.isRemoteNewer("1.0", "v1.0.0"))
        assertFalse(SemanticVersionComparator.isRemoteNewer("1.0.1", "v1.0.1"))
        assertFalse(SemanticVersionComparator.isRemoteNewer("1.2.0", "v1.1.9"))
        assertFalse(SemanticVersionComparator.isRemoteNewer("1.2.0", "1.2.0"))

        assertTrue(SemanticVersionComparator.isRemoteNewer("1.0", "v1.0.1"))
        assertTrue(SemanticVersionComparator.isRemoteNewer("1.0.1", "v1.0.2"))
        assertTrue(SemanticVersionComparator.isRemoteNewer("1.0", "v1.1.0"))
        assertTrue(SemanticVersionComparator.isRemoteNewer("1.2.0", "v1.10.0"))
        assertTrue(SemanticVersionComparator.isRemoteNewer("1.0.0-beta1", "v1.0.0"))
        assertFalse(SemanticVersionComparator.isRemoteNewer("1.0.0", "v1.0.0-rc1"))
    }

    @Test
    fun `normalizeOwnerRepoInput normalizes urls and slugs`() {
        assertEquals(
            "gokulan/loop-android",
            SemanticVersionComparator.normalizeOwnerRepoInput("gokulan/loop-android")
        )
        assertEquals(
            "gokulan/loop-android",
            SemanticVersionComparator.normalizeOwnerRepoInput("https://github.com/gokulan/loop-android")
        )
        assertEquals(
            "gokulan/loop-android",
            SemanticVersionComparator.normalizeOwnerRepoInput("https://github.com/gokulan/loop-android/releases/latest")
        )
        assertEquals(
            "gokulan/loop-android",
            SemanticVersionComparator.normalizeOwnerRepoInput("github.com/gokulan/loop-android.git")
        )
        assertNull(SemanticVersionComparator.normalizeOwnerRepoInput(""))
        assertNull(SemanticVersionComparator.normalizeOwnerRepoInput("invalid-repo-only"))
    }
}
