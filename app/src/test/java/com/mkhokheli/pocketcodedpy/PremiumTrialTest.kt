package com.mkhokheli.pocketcodedpy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumTrialTest {
    @Test
    fun trialExpiresAtExactDeadline() {
        assertFalse(isTrialExpired(now = 9_999L, trialEndsAt = 10_000L))
        assertTrue(isTrialExpired(now = 10_000L, trialEndsAt = 10_000L))
    }

    @Test
    fun firstReminderIsOneHourAfterTrialStart() {
        val hour = 60L * 60L * 1000L
        assertEquals(5_000L + hour, nextTrialReminderAt(5_000L, 0L))
    }

    @Test
    fun continuationMovesReminderForwardByOneHour() {
        val hour = 60L * 60L * 1000L
        assertEquals(20_000L + hour, nextTrialReminderAt(5_000L, 20_000L))
    }
}
