package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumTrialTest {
    private val hourMs = 60L * 60L * 1_000L
    private val trialMs = 72L * hourMs

    @Test
    fun trialExpiresAtEndTime() {
        assertFalse(isTrialExpired(now = 999L, trialEndsAt = 1_000L))
        assertTrue(isTrialExpired(now = 1_000L, trialEndsAt = 1_000L))
        assertTrue(isTrialExpired(now = 1_001L, trialEndsAt = 1_000L))
    }

    @Test
    fun reminderUsesTrialStartBeforeAnyContinuation() {
        assertEquals(
            hourMs,
            nextTrialReminderAt(trialStartedAt = 0L, lastContinuationAt = 0L),
        )
    }

    @Test
    fun reminderUsesLastContinuationAfterTrialStarts() {
        assertEquals(
            2L * hourMs,
            nextTrialReminderAt(
                trialStartedAt = 0L,
                lastContinuationAt = hourMs,
            ),
        )
    }

    @Test
    fun trialGateStaysHiddenBeforeFirstHourlyReminder() {
        assertFalse(
            shouldShowTrialGate(
                now = hourMs - 1L,
                trialStartedAt = 0L,
                lastContinuationAt = 0L,
                isPremium = false,
            )
        )
    }

    @Test
    fun trialGateShowsAtHourlyReminderForNonPremiumUsers() {
        assertTrue(
            shouldShowTrialGate(
                now = hourMs,
                trialStartedAt = 0L,
                lastContinuationAt = 0L,
                isPremium = false,
            )
        )
    }

    @Test
    fun trialGateShowsAfterTrialExpiresForNonPremiumUsers() {
        assertTrue(
            shouldShowTrialGate(
                now = trialMs,
                trialStartedAt = 0L,
                lastContinuationAt = 71L * hourMs,
                isPremium = false,
            )
        )
    }

    @Test
    fun premiumUsersBypassTrialGateAfterTrialExpires() {
        assertFalse(
            shouldShowTrialGate(
                now = trialMs + hourMs,
                trialStartedAt = 0L,
                lastContinuationAt = 0L,
                isPremium = true,
            )
        )
    }
}
