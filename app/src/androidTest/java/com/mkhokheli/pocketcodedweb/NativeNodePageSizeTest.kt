package com.mkhokheli.pocketcodedweb

import android.system.Os
import android.system.OsConstants
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeNodePageSizeTest {
    @Test fun runtimeExecutesAndRecoversOnDevicePageSize() {
        val pageSize = Os.sysconf(OsConstants._SC_PAGESIZE)
        Log.i("NativeNodePageSize", "Device page size: $pageSize")
        InstrumentationRegistry.getArguments().getString("expectedPageSize")?.let {
            assertEquals("Wrong device for page-size validation", it.toLong(), pageSize)
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        NativeNodeRuntime.initialize(context)
        val root = File(context.filesDir, "node-page-size-test").apply { mkdirs() }
        try {
            val result = NativeNodeRuntime.evaluate(root, root, """
                const assert = require('assert');
                const fs = require('fs');
                const path = require('path');
                const crypto = require('crypto');
                assert.strictEqual(process.version, 'v18.20.4');
                const bytes = Buffer.alloc(65536, 42);
                const file = path.join(process.cwd(), 'page-test.bin');
                fs.writeFileSync(file, bytes);
                assert.strictEqual(fs.readFileSync(file).length, 65536);
                assert.strictEqual(crypto.createHash('sha256').update('abc').digest('hex'),
                  'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad');
                assert.strictEqual(new WebAssembly.Memory({initial: 1}).buffer.byteLength, 65536);
                assert.strictEqual(Array.from({length: 10000}, (_, i) => i).reduce((a,b) => a+b), 49995000);
                console.log('NATIVE_PAGE_SIZE_OK');
            """.trimIndent(), 15_000)
            assertTrue(result.stderr, result.succeeded)
            assertTrue(result.stdout, result.stdout.contains("NATIVE_PAGE_SIZE_OK"))
            val timeout = NativeNodeRuntime.evaluate(root, root, "while (true) {}", 750)
            assertTrue(timeout.stderr, timeout.timedOut)
            val recovered = NativeNodeRuntime.evaluate(root, root, "console.log(6 * 7)", 5_000)
            assertTrue(recovered.stderr, recovered.succeeded)
            assertTrue(recovered.stdout, recovered.stdout.contains("42"))
        } finally {
            root.deleteRecursively()
        }
    }
}
