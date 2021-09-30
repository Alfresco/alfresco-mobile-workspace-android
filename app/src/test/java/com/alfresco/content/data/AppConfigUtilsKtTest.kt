package com.alfresco.content.data

import org.junit.Assert.assertEquals
import org.junit.Test

internal class AppConfigUtilsKtTest {

    @Test
    fun previous_time_passed_24_hours() {

        val diffHours = (1 * 25 * 60 * 60 * 1000)
        val testTime = System.currentTimeMillis() - diffHours
        val result = isTimeToFetchConfig(testTime)
        assertEquals(result, true)
    }

    @Test
    fun previous_time_not_passed_24_hours() {

        val diffHours = (1 * 22 * 60 * 60 * 1000)
        val testTime = System.currentTimeMillis() - diffHours
        val result = isTimeToFetchConfig(testTime)
        assertEquals(result, false)
    }
}
