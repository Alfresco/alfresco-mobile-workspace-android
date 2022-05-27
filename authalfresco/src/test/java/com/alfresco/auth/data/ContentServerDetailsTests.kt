package com.alfresco.auth.data

import org.junit.Test

class ContentServerDetailsTests {
    @Test
    fun `version 6-2 greater than 5-2-2`() {
        assert(testData("6.2").isAtLeast("5.2.2"))
    }

    @Test
    fun `version 5-2-3 greater than 5-2`() {
        assert(testData("5.2.3").isAtLeast("5.2"))
    }

    @Test
    fun `version 5-2-1 greater than 5-2-2`() {
        assert(testData("5.2.3").isAtLeast("5.2"))
    }

    @Test
    fun `version 5-2 smaller than 5-2-2`() {
        assert(!testData("5.2").isAtLeast("5.2.2"))
    }

    @Test
    fun `version 5-2-0 at least to 5-2`() {
        assert(testData("5.2.0").isAtLeast("5.2"))
    }

    @Test
    fun `version 5-2 at least to 5-2-0`() {
        assert(testData("5.2").isAtLeast("5.2.0"))
    }

    @Test
    fun `version 6-2 (120) at least to 5-2-0`() {
        assert(testData("6.2 (b120)").isAtLeast("5.2.2"))
    }

    @Test
    fun `version 5-2-0 at least to 5-2-0 (b120)`() {
        assert(testData("5.2.0").isAtLeast("5.2.0 (b120)"))
    }

    @Test
    fun `version invalid not greater 5-2-0`() {
        assert(!testData("invalid").isAtLeast("5.2.0 (b120)"))
    }

    private fun testData(version: String): ContentServerDetails {
        return ContentServerDetails(ContentServerDetailsData(EDITION, version, SCHEMA))
    }

    companion object {
        const val EDITION = "community"
        const val SCHEMA = "100"
    }
}
