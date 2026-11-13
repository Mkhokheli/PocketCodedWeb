package com.mkhokheli.pocketcodedweb

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.mkhokheli.pocketcodedweb.ui.theme.PocketCodedWebTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FreemiumDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.getExternalFilesDir(null), "freemium-tests").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun freeTerminalCopiesOutputAndAllowsOnlyRequestedProgramInput() {
        var awaiting by mutableStateOf(false)
        var input by mutableStateOf("")
        var submitted = ""
        compose.setContent {
            PocketCodedWebTheme(true, WebTheme.VSCodeDefaultDark.name, false) {
                CompositionLocalProvider(LocalFeatureAccess provides FeatureAccess(false)) {
                    Column(Modifier.fillMaxSize()) {
                        WebTerminalMenu("Program output: hello", webIdePalette(WebTheme.VSCodeDefaultDark))
                        TerminalBody(terminalText = "Program output: hello", terminalInput = input, promptText = "$", palette = webIdePalette(WebTheme.VSCodeDefaultDark),
                            isRunning = awaiting, keepInputVisibleWhileRunning = true, isAwaitingInput = awaiting,
                            modifier = Modifier.weight(1f), onTerminalFocusChange = {},
                            onTerminalInputChange = { input = it }, onTerminalCommandSubmit = { submitted = input })
                    }
                }
            }
        }
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        compose.onNodeWithContentDescription("Terminal tools").performClick()
        compose.onNodeWithText("Copy output").performClick()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        compose.runOnIdle { awaiting = true }
        compose.onNode(hasSetTextAction()).performTextInput("Ada")
        compose.onNode(hasSetTextAction()).performImeAction()
        compose.runOnIdle { assertEquals("Ada", submitted) }
    }

    @Test fun yearlyPlanUsesItsLocalizedPriceAndShowsFinalDayWarning() {
        var selected: PremiumPlan? = null
        compose.setContent {
            PocketCodedWebTheme(true, WebTheme.VSCodeDefaultDark.name, false) {
                PremiumPaywall(entryPalette(WebTheme.VSCodeDefaultDark.name, true), 1, false,
                    BillingUiState(connected = true, productAvailable = true, formattedPrice = "R319.99",
                        monthlyAvailable = true, monthlyPrice = "R29.99", yearlyAvailable = true, yearlyPrice = "R199.99"),
                    onBuy = { selected = PremiumPlan.Lifetime }, onSubscribe = { selected = PremiumPlan.Monthly },
                    onYearly = { selected = PremiumPlan.Yearly }, onRetry = {}, onRestore = {}, onContinueTrial = {})
            }
        }
        compose.onNodeWithText("1 day left: keep access to your projects").performScrollTo().assertIsDisplayed()
        screenshot("premium-dark-warning")
        compose.onNodeWithText("Yearly", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithText("Continue - R199.99").performScrollTo().assertIsEnabled()
        screenshot("premium-dark-yearly")
        compose.onNodeWithText("Continue - R199.99").performClick()
        compose.runOnIdle { assertEquals(PremiumPlan.Yearly, selected) }
    }

    @Test fun unavailableYearlyPlanDoesNotBorrowAnotherPlansPrice() {
        compose.setContent {
            PocketCodedWebTheme(false, WebTheme.VSCodeDefaultLight.name, false) {
                PremiumPaywall(entryPalette(WebTheme.VSCodeDefaultLight.name, false), 0, true,
                    BillingUiState(connected = true, productAvailable = true, formattedPrice = "R319.99",
                        monthlyAvailable = true, monthlyPrice = "R29.99"),
                    onBuy = {}, onSubscribe = {}, onYearly = {}, onRetry = {}, onRestore = {}, onContinueTrial = {})
            }
        }
        compose.onNodeWithText("Yearly", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithText("Plan unavailable").performScrollTo().assertIsNotEnabled()
        screenshot("premium-light-yearly-unavailable")
        compose.onNodeWithText("Monthly", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithText("Continue - R29.99").performScrollTo().assertIsEnabled()
    }
}
