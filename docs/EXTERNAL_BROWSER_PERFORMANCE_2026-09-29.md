# External browser preview performance — September 29–30, 2026

## Scope

Framework-independent host improvements for websites opened in external browsers while a persistent Node development server runs. Actual Node HTTP responses go directly to the browser; PocketCoded does not rewrite project JavaScript, CSS, development settings or HMR.

## Changes

- Running Node sessions flush bounded log snapshots asynchronously, with at most one disk write in flight. New messages accumulate in the existing bounded buffer instead of queuing disk writes. A complete snapshot is published with a rename. Errors retry on the next flush; finite programs and failures synchronously preserve the final output. A generation check prevents an older callback from replacing final output.
- Embedded previews pause WebView timers and rendering when the activity stops, so an embedded copy does not keep executing JavaScript alongside Chrome. Returning to the app resumes the preview. Disposal restores process-wide timers before destroying the view. The separate Node service stays running.
- External server previews mark the Node session as browser-facing. On devices classified
  as constrained by `DeviceEfficiency`, the Node session thread is scheduled at background
  priority while Chrome is in use, leaving more CPU headroom for browser rendering. Normal
  and high-memory devices retain the default scheduler priority because the connected-phone
  benchmark showed no reliable benefit from lowering it on a 5.4 GiB device.

Android documents that `WebView.onPause()` alone does not pause JavaScript, while `pauseTimers()` affects all WebViews in the app process: [WebView reference](https://developer.android.com/reference/android/webkit/WebView). The controller therefore pairs pause/resume and restores timers during disposal.

## Chrome baseline on the connected phone

Xiaomi 2209116AG, Android 13, approximately 5.4 GiB RAM. The test opens real Chrome, serves a synthetic 6 MB script (mostly generated documentation, **not** a real framework build), creates 2,000 cards and animates 80 with transforms and background-color painting. Each phase measures 10 seconds after a 1.5-second warmup. The busy phase deliberately spends approximately 6 ms every 20 ms computing and emits approximately 2,500 log messages per second. API requests run every 100 ms.

Baseline before these changes, September 29 at 13:50:

| Metric | Idle server | Busy server |
| --- | ---: | ---: |
| Browser frame interval, median | 33.3 ms | 33.3 ms |
| Browser frame interval, p95 | 50.0 ms | 66.8 ms |
| Frames sampled | 307 | 258 |
| Browser long tasks | 0 | 4 |
| Browser API round trip, p95 | 49.3 ms | 55.3 ms |
| Node CPU, one-core equivalent | 5.0% | 42.5% |
| Node event-loop delay, p95 | 11.0 ms | 18.8 ms |

The deliberately paint-heavy page is already slow with an idle backend; eliminating host overhead cannot by itself make arbitrary site animations smooth. This phone is not a 2 GB device. Temperature and other background activity are uncontrolled, so a single timing run cannot establish a general speedup.

## Validation

All eight host Node tests pass, including HTTP/ESM behavior, error reporting, bounded logs, zero synchronous log-content writes during an active session, final output during an interrupted flush, and serialized writes under simulated slow storage. Android application and instrumentation APK compilation and native alignment validation passed.

The September 30 retry installed successfully. Both physical-phone tests passed with zero failures, including real-WebView JavaScript timer suspension in the background, resumption on return, and restoration of process-wide timers on disposal. Compilation and native alignment checks passed again. Earlier installation attempts had been blocked by `INSTALL_FAILED_USER_RESTRICTED`.

Post-change Chrome results, September 30 at 12:40, with the same workload:

| Metric | Idle server | Busy server |
| --- | ---: | ---: |
| Browser frame interval, median | 41.5 ms | 33.4 ms |
| Browser frame interval, p95 | 75.0 ms | 58.3 ms |
| Frames sampled | 239 | 275 |
| Browser long tasks | 8 | 3 |
| Browser API round trip, p95 | 65.7 ms | 69.2 ms |
| Node CPU, one-core equivalent | 5.6% | 42.4% |
| Node event-loop delay, p95 | 12.8 ms | 18.8 ms |

These results are mixed: busy-server frame p95 improved, idle frames and API latency worsened. They do **not** establish a causal or general browser speedup. The retained changes eliminate synchronous log-content writes during active sessions, independently verified hidden-preview JavaScript activity, and apply scheduler relief only to constrained devices; they do not resolve all jank in this deliberately paint-heavy page. The Chrome benchmark itself does not mount an embedded preview, so it cannot measure the timer-pause change's benefit. Raw post-change metrics are saved in `.codex-tmp/external-browser-performance-2026-09-30.log`.

## Final connected-device validation — 22 October 2026

The updated debug APK and instrumentation APK were installed on the connected Xiaomi
2209116AG (Android 13). Both external-browser tests passed:

- Chrome animation benchmark: passed; the page remained visible and reported 6,000 modules.
- Embedded-preview lifecycle test: passed; timers paused in the background and resumed after
  returning or disposing the preview.

The final benchmark reported idle and busy frame p95 of 75 ms, busy API p95 of 64.9 ms, and
four idle long tasks. These results are materially better than the earlier 125 ms idle p95
sample from the unrestricted scheduler experiment, but still do not make the deliberately
paint-heavy synthetic page a 60 fps workload. Scheduler relief is therefore restricted to
devices classified as constrained; it is not enabled on the tested 5.4 GiB phone.

Reproduce with `node --test tools/test-node-session.cjs` and Android test class `com.mkhokheli.pocketcodedweb.ExternalBrowserPerformanceTest`. Chrome metrics are logged under `PocketCodedChrome`. Keep the phone unlocked and the benchmark tab visible. The benchmark navigates its own tab to `about:blank` when finished and stops its Node session in `finally`. The isolated test application uses `.codex-tmp/node-session-test.init.gradle` to avoid replacing the user's differently signed installation.
