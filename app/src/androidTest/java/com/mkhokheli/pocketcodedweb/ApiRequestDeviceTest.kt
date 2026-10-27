package com.mkhokheli.pocketcodedweb

import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ApiRequestDeviceTest {
    private val server = ApiTestServer()
    @After fun cleanup() = server.close()

    @Test fun allSevenMethodsReachTheServerWithCorrectPayloadAndHeaders() {
        for (method in API_METHODS) {
            val body = "{\"message\":\"hello \\u2603\",\"number\":42}"
            val response = ApiRequestCall().execute(ApiRequest(method, "${server.url}/echo?q=a%20b",
                "X-Test-Token: device-secret\nContent-Type: application/json", body))
            assertEquals(method, 200, response.status)
            assertTrue(response.headers.contains("x-api-test: phone", true))
            if (method == "HEAD") assertEquals("", response.body)
            else {
                val json = JSONObject(response.body)
                assertEquals(method, json.getString("method"))
                assertEquals("device-secret", json.getString("token"))
                assertEquals(if (method == "GET") "" else body, json.getString("body"))
                assertEquals("/echo?q=a%20b", json.getString("path"))
            }
        }
    }
    @Test fun errorResponseRetainsBodyAndHeaders() {
        val response = ApiRequestCall().execute(ApiRequest("GET", "${server.url}/error"))
        assertEquals(422, response.status); assertTrue(JSONObject(response.body).getBoolean("error"))
        assertTrue(response.headers.contains("x-api-test", true))
    }
    @Test fun emptyAndHeadResponsesDoNotHang() {
        assertEquals("", ApiRequestCall().execute(ApiRequest("GET", "${server.url}/empty")).body)
        assertEquals("", ApiRequestCall().execute(ApiRequest("HEAD", "${server.url}/echo")).body)
    }
    @Test fun redirectIsVisibleWithoutFollowingIt() {
        val result = ApiRequestCall().execute(ApiRequest("GET", "${server.url}/redirect"))
        assertEquals(302, result.status); assertTrue(result.headers.contains("location: /echo", true))
    }
    @Test fun responseCharsetAndUnicodeRequestArePreserved() {
        assertEquals("caf\u00e9", ApiRequestCall().execute(ApiRequest("GET", "${server.url}/charset")).body)
        val text = "h\u00e9llo \u2603 \ud83d\ude00"
        val response = ApiRequestCall().execute(ApiRequest("POST", "${server.url}/echo", body = text))
        assertEquals(text, JSONObject(response.body).getString("body"))
    }
    @Test fun hugeResponseIsBounded() {
        val response = ApiRequestCall().execute(ApiRequest("GET", "${server.url}/large"))
        assertTrue(response.truncated); assertEquals(API_RESPONSE_LIMIT, response.bytes)
        assertEquals(API_RESPONSE_LIMIT, response.body.length)
    }
    @Test fun readTimeoutIsEnforced() {
        val error = runCatching { ApiRequestCall().execute(ApiRequest("GET", "${server.url}/slow", timeoutSeconds = 1)) }.exceptionOrNull()
        assertTrue(error.toString(), error is SocketTimeoutException)
    }
    @Test fun cancelInterruptsWorkAndNextRequestWorks() {
        val pool = Executors.newSingleThreadExecutor()
        try {
            val call = ApiRequestCall()
            val future = pool.submit<ApiResponse> { call.execute(ApiRequest("GET", "${server.url}/slow")) }
            Thread.sleep(150)
            call.cancel()
            val failure = runCatching { future.get(2, TimeUnit.SECONDS) }.exceptionOrNull()
            assertTrue(failure.toString(), failure is java.util.concurrent.ExecutionException)
            assertEquals(200, ApiRequestCall().execute(ApiRequest("GET", "${server.url}/echo")).status)
        } finally { pool.shutdownNow() }
    }
    @Test fun malformedUrlsHeadersTimeoutAndOversizedBodyAreRejected() {
        val invalid = listOf(
            ApiRequest("GET", "file:///etc/passwd"), ApiRequest("GET", "http://"),
            ApiRequest("GET", "https://user:password@example.com"), ApiRequest("GET", "http://localhost:99999"),
            ApiRequest("GET", server.url, "Invalid header"), ApiRequest("GET", server.url, "Bad name: value"),
            ApiRequest("GET", server.url, "Content-Length: 99"), ApiRequest("GET", server.url, timeoutSeconds = 0),
            ApiRequest("POST", server.url, body = "x".repeat(API_RESPONSE_LIMIT + 1)),
        )
        invalid.forEach { assertTrue(it.toString().take(100), runCatching { it.validate() }.isFailure) }
    }
    @Test fun thirtySequentialRequestsDoNotLeakOrMixResponses() {
        repeat(30) { index ->
            val response = ApiRequestCall().execute(ApiRequest("POST", "${server.url}/echo", body = "request-$index"))
            assertEquals("request-$index", JSONObject(response.body).getString("body"))
        }
    }
    @Test fun refusedConnectionReportsFailure() {
        val port = java.net.ServerSocket(0).use { it.localPort }
        assertTrue(runCatching { ApiRequestCall().execute(ApiRequest("GET", "http://127.0.0.1:$port", timeoutSeconds = 1)) }.isFailure)
    }
    @Test fun realHttpsEndpointUsesPlatformTls() {
        val response = ApiRequestCall().execute(ApiRequest("GET", "https://example.com", timeoutSeconds = 20))
        assertEquals(200, response.status)
        assertTrue(response.body.contains("Example Domain"))
    }
    @Test fun prettyFormattingHandlesObjectsArraysAndNonJson() {
        assertTrue(prettyApiBody("{\"a\":1}").contains("\n"))
        assertTrue(prettyApiBody("[1,2]").contains("\n"))
        assertEquals("<html>error</html>", prettyApiBody("<html>error</html>"))
        assertEquals("{invalid", prettyApiBody("{invalid"))
    }
}
