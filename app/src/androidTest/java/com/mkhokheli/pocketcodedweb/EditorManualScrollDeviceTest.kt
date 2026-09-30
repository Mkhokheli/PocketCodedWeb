package com.mkhokheli.pocketcodedweb

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class EditorManualScrollDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun manualScrollingWorksButRevealRequestsDoNotMoveEditorOrPage() {
        lateinit var editor: ScrollState
        lateinit var page: ScrollState
        lateinit var requester: BringIntoViewRequester
        lateinit var scope: CoroutineScope
        compose.setContent {
            editor = rememberScrollState()
            page = rememberScrollState()
            requester = remember { BringIntoViewRequester() }
            scope = rememberCoroutineScope()
            Column(Modifier.size(300.dp).verticalScroll(page)) {
                ManualScrollEditor(Modifier.height(200.dp)) {
                    Column(Modifier.fillMaxWidth().height(200.dp).verticalScroll(editor).testTag("editor-scroll")) {
                        Spacer(Modifier.height(1500.dp))
                        Box(Modifier.size(40.dp).bringIntoViewRequester(requester))
                    }
                }
                Spacer(Modifier.height(1000.dp))
            }
        }
        compose.runOnIdle { scope.launch { requester.bringIntoView() } }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0, editor.value)
            assertEquals(0, page.value)
        }
        compose.onNodeWithTag("editor-scroll").performTouchInput { swipeUp() }
        compose.waitForIdle()
        var manualPosition = 0
        compose.runOnIdle {
            manualPosition = editor.value
            assertTrue("Manual scrolling must remain enabled", manualPosition > 0)
            scope.launch { requester.bringIntoView() }
        }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(manualPosition, editor.value)
            assertEquals(0, page.value)
        }
    }
}
