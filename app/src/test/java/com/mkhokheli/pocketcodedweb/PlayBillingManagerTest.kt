package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayBillingManagerTest {
    @Test
    fun googlePlayOrderIdsMustUseGpaPrefix() {
        assertTrue(isGooglePlayOrderId("GPA.1234-5678-9012-34567"))
        assertFalse(isGooglePlayOrderId(null))
        assertFalse(isGooglePlayOrderId(""))
        assertFalse(isGooglePlayOrderId("TEST.1234"))
    }

    @Test
    fun purchaseAccountMustMatchExpectedObfuscatedAccountId() {
        assertTrue(purchaseAccountMatchesExpected("abc123", "abc123"))
        assertFalse(purchaseAccountMatchesExpected("abc123", null))
        assertFalse(purchaseAccountMatchesExpected("abc123", "other"))
    }

    @Test
    fun purchaseTimeCanUseOneDayGraceAroundTrialStart() {
        val dayMs = 24L * 60L * 60L * 1000L
        val trialStart = 10L * dayMs

        assertTrue(purchaseTimeFitsAccount(trialStart, trialStart))
        assertTrue(purchaseTimeFitsAccount(trialStart - dayMs, trialStart))
        assertFalse(purchaseTimeFitsAccount(trialStart - dayMs - 1L, trialStart))
        assertFalse(purchaseTimeFitsAccount(0L, trialStart))
    }
}
