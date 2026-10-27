package com.mkhokheli.pocketcodedweb

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException

internal class ApiTesterState(private val scope: CoroutineScope) {
    var open by mutableStateOf(false)
    var method by mutableStateOf("GET")
    var url by mutableStateOf("")
    var headers by mutableStateOf("Accept: application/json")
    var body by mutableStateOf("")
    var timeout by mutableStateOf("15")
    var running by mutableStateOf(false)
        private set
    var response by mutableStateOf<ApiResponse?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var tab by mutableStateOf("Request")
    private var call: ApiRequestCall? = null
    private var job: Job? = null

    fun send() {
        if (running) return
        val request = ApiRequest(method, url, headers, body, timeout.toIntOrNull() ?: 0)
        runCatching { request.validate() }.onFailure { error = it.message; return }
        val activeCall = ApiRequestCall()
        call = activeCall
        error = null
        response = null
        running = true
        job = scope.launch {
            try {
                val result = withContext(Dispatchers.IO) { activeCall.execute(request) }
                response = result
                tab = "Response"
            } catch (canceled: CancellationException) {
                throw canceled
            } catch (failure: Exception) {
                ensureActive()
                if (call !== activeCall) return@launch
                error = when (failure) {
                    is SocketTimeoutException -> "Request timed out. Check the server or increase the timeout."
                    is javax.net.ssl.SSLException -> "TLS connection failed. Check the server certificate."
                    else -> failure.message?.take(300) ?: "Could not connect to the server."
                }
            } finally {
                if (call === activeCall) { running = false; call = null }
            }
        }
    }

    fun cancel() {
        val activeCall = call
        call = null
        job?.cancel()
        job = null
        if (running) error = "Request canceled."
        running = false
        activeCall?.cancel()
    }
    fun close() { cancel(); open = false }
}

@Composable
internal fun ApiTesterContent(
    state: ApiTesterState, modifier: Modifier = Modifier,
    expanded: Boolean = false, onExpand: () -> Unit = {},
) {
    val colors = ideToolColors()
    val keyboard = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboardManager.current
    var methodMenu by remember { mutableStateOf(false) }
    var showResponseHeaders by remember { mutableStateOf(false) }
    var pretty by remember { mutableStateOf(true) }
    BackHandler { state.close() }
    Column(modifier.testTag("api-tester")) {
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("API tester", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            IconButton(onClick = onExpand, modifier = Modifier.size(48.dp)) {
                Icon(if (expanded) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                    if (expanded) "Minimize API tester" else "Fullscreen API tester", tint = colors.text)
            }
            IconButton(onClick = { state.close() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Rounded.Close, "Close API tester", tint = colors.text)
            }
        }
        HorizontalDivider(color = colors.border)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            IdeToolTextField(state.url, { state.url = it }, "Request URL",
                modifier = Modifier.fillMaxWidth().testTag("api-url"), singleLine = true)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    IdeToolOutlinedButton(onClick = { methodMenu = true }, enabled = !state.running,
                        modifier = Modifier.widthIn(min = 112.dp).heightIn(min = 48.dp)) {
                        Text(state.method)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Rounded.ArrowDropDown, "Choose HTTP method", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = methodMenu, onDismissRequest = { methodMenu = false }, containerColor = colors.surface) {
                        API_METHODS.forEach { method -> DropdownMenuItem(text = { Text(method, color = colors.text) },
                            onClick = { state.method = method; methodMenu = false }) }
                    }
                }
                if (state.running) {
                    IdeToolOutlinedButton(onClick = { state.cancel() },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                        Text("Cancel request")
                    }
                } else {
                    IdeToolPrimaryButton(onClick = { keyboard?.hide(); state.send() }, runAction = true,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                        Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Send request")
                    }
                }
            }
            state.error?.let { Text(it, color = colors.error, modifier = Modifier.testTag("api-error")) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Request", "Response").forEach { label ->
                    IdeToolOutlinedButton(onClick = { state.tab = label }, selected = state.tab == label,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(label) }
                }
            }
            if (state.tab == "Request") {
                IdeToolTextField(state.headers, { state.headers = it }, "Headers (Name: value, one per line)",
                    modifier = Modifier.fillMaxWidth().testTag("api-headers"), minLines = 2)
                if (state.method !in listOf("GET", "HEAD")) {
                    IdeToolTextField(state.body, { state.body = it }, "Request body",
                        modifier = Modifier.fillMaxWidth().testTag("api-body"), minLines = 3)
                    Text("Body is sent as UTF-8. Default Content-Type: application/json; override it in Headers.",
                        color = colors.mutedText, fontSize = 12.sp)
                }
                IdeToolTextField(state.timeout, { state.timeout = it }, "Timeout (seconds, 1–120)", singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("api-timeout"))
                Text("Redirects are shown without following them. Requests and credentials stay in memory for this workspace session.",
                    color = colors.mutedText, fontSize = 12.sp)
            } else {
                val response = state.response
                if (response == null) Text(if (state.running) "Waiting for response..." else "Send a request to inspect the response.", color = colors.mutedText)
                else {
                    Text("${response.status} ${response.reason}", fontWeight = FontWeight.SemiBold,
                        color = if (response.status in 200..299) colors.success else colors.warning,
                        modifier = Modifier.testTag("api-status"))
                    Text("${response.elapsedMillis} ms · ${response.bytes} bytes", color = colors.mutedText, fontSize = 12.sp)
                    if (response.truncated) Text("Response limited to the first 1 MB.", color = colors.warning)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IdeToolOutlinedButton(onClick = { showResponseHeaders = false }, selected = !showResponseHeaders,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Body") }
                        IdeToolOutlinedButton(onClick = { showResponseHeaders = true }, selected = showResponseHeaders,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text("Headers") }
                    }
                    val formattedBody = remember(response, pretty) { if (pretty) prettyApiBody(response.body) else response.body }
                    val displayed = if (showResponseHeaders) response.headers else formattedBody
                    if (displayed.length > 32_000) Text("Preview shows the first 32,000 characters. Copy response includes the full captured response.", color = colors.warning, fontSize = 12.sp)
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!showResponseHeaders) IdeToolOutlinedButton(onClick = { pretty = !pretty }, selected = pretty,
                            modifier = Modifier.heightIn(min = 48.dp)) { Text("Pretty JSON") }
                        IdeToolOutlinedButton(onClick = { clipboard.setText(AnnotatedString(displayed)) },
                            enabled = displayed.isNotEmpty(), modifier = Modifier.heightIn(min = 48.dp)) { Text("Copy response") }
                    }
                    SelectionContainer {
                        Text(displayed.take(32_000).ifEmpty { "(empty body)" }, color = colors.text, fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp, modifier = Modifier.fillMaxWidth().background(colors.codeBackground, IdeToolControlShape)
                                .padding(10.dp).testTag("api-response"))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

internal fun prettyApiBody(body: String): String {
    // Large responses remain raw to keep formatting work off the interactive path.
    if (body.length > 100_000) return body
    return runCatching {
        when {
            body.trimStart().startsWith("{") -> JSONObject(body).toString(2)
            body.trimStart().startsWith("[") -> JSONArray(body).toString(2)
            else -> body
        }
    }.getOrDefault(body)
}
