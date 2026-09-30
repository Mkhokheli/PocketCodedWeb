package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumTrialTest {
    @Test
    fun earliestTrialStartNeverExtendsExistingTrial() {
        assertEquals(100L, earliestTrialStart(localStart = 100L, serverStart = 200L))
        assertEquals(100L, earliestTrialStart(localStart = 200L, serverStart = 100L))
        assertEquals(200L, earliestTrialStart(localStart = 0L, serverStart = 200L))
    }

    private val hourMs = 60L * 60L * 1_000L
    private val dayMs = 24L * hourMs
    private val trialMs = 14L * dayMs

    @Test
    fun trialExpiresAtEndTime() {
        assertFalse(isTrialExpired(now = 999L, trialEndsAt = 1_000L))
        assertTrue(isTrialExpired(now = 1_000L, trialEndsAt = 1_000L))
        assertTrue(isTrialExpired(now = 1_001L, trialEndsAt = 1_000L))
    }

    @Test
    fun reminderUsesTrialStartBeforeAnyContinuation() {
        assertEquals(
            dayMs,
            nextTrialReminderAt(trialStartedAt = 0L, lastContinuationAt = 0L),
        )
    }

    @Test
    fun reminderUsesLastContinuationAfterTrialStarts() {
        assertEquals(
            2L * dayMs,
            nextTrialReminderAt(
                trialStartedAt = 0L,
                lastContinuationAt = dayMs,
            ),
        )
    }

    @Test
    fun trialGateStaysHiddenBeforeFirstDailyReminder() {
        assertFalse(
            shouldShowTrialGate(
                now = dayMs - 1L,
                trialStartedAt = 0L,
                lastContinuationAt = 0L,
                isPremium = false,
            )
        )
    }

    @Test
    fun trialGateShowsAtDailyReminderForNonPremiumUsers() {
        assertTrue(
            shouldShowTrialGate(
                now = dayMs,
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
                lastContinuationAt = 13L * dayMs,
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

    @Test
    fun countdownRoundsUpToWholeDaysAndReachesZeroAtExpiry() {
        assertEquals(14L, trialDaysRemaining(now = 0L, trialEndsAt = trialMs))
        assertEquals(13L, trialDaysRemaining(now = dayMs, trialEndsAt = trialMs))
        assertEquals(1L, trialDaysRemaining(now = trialMs - 1L, trialEndsAt = trialMs))
        assertEquals(0L, trialDaysRemaining(now = trialMs, trialEndsAt = trialMs))
    }
}
