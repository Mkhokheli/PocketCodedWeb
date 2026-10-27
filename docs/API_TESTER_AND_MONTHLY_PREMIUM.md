# API tester and monthly Premium

Open a project and choose **Editor tools > API tester** from the code editor menu.
The tester replaces the editor card temporarily, follows the selected Web theme, and supports
compact/fullscreen layouts. Closing it restores the editor and previous explorer visibility.

Use GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS; custom headers; UTF-8 request bodies;
JSON formatting; response headers; copying; timing; cancellation; and a 1?120 second timeout.
Captured responses are limited to 1 MiB; the preview shows up to 32,000 characters.
Requests remain in memory for the workspace session. They are not written to project files.

## Test your own Node.js API

Copy `docs/examples/api-server.cjs` into a Web project. Run `node api-server.cjs` in the terminal,
keep the server running, then open API tester. Send GET to `http://127.0.0.1:8000/api/health`.
To test a body, send POST to `http://127.0.0.1:8000/api/echo` with a JSON request body.
Stop the server from the terminal when finished. No npm dependency is required for this example.

## Monthly Premium

Monthly Premium is shown above lifetime. Product ID: `pocketcodedweb_premium_monthly`.
Base plan ID: `monthly`; auto-renewing; billing period: monthly. Price comes from Google Play.
For Console fields, RTDN, backend deployment, and purchase testing, see `functions/README.md`.
The monthly price and regional availability must be chosen and the base plan activated in Console.

## Native crash symbols

Release builds request FULL native debug symbols and include available symbols in the App Bundle.
Third-party binaries may already be stripped; symbols cannot be reconstructed for those binaries.
The existing 16 KB native alignment checks remain in place.

## Local testing

The debug app uses `com.mkhokheli.pocketcodedweb.debug` so it can coexist with the Play-signed app.
Firebase debug configuration is only for local development; register the debug signing certificate
and App Check debug token before using authenticated services in that build. Subscription checkout
must be tested with the production package on a Play internal testing track using license testers.

Run backend lifecycle tests: `node --test functions/subscriptions.test.js`.
Run app unit tests: `.\gradlew.bat :app:testDebugUnitTest --offline`.
Instrumentation classes: `ApiRequestDeviceTest` and `ApiTesterUiDeviceTest`.
