package com.alfresco.content.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

internal class AppConfigUtilsKtTest {
    @Test
    fun previous_time_passed() {
        val previousFetchTime = TimeUnit.HOURS.toMillis(25)
        val testTime = System.currentTimeMillis() - previousFetchTime
        val result = isTimeToFetchConfig(testTime)
        assertEquals(result, true)
    }

    @Test
    fun previous_time_not_passed() {
        val previousFetchTime = TimeUnit.HOURS.toMillis(22)
        val testTime = System.currentTimeMillis() - previousFetchTime
        val result = isTimeToFetchConfig(testTime)
        assertEquals(result, false)
    }
}
