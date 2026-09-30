import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.mkhokheli.pocketcodedweb"
    compileSdk = 36
    ndkVersion = "29.0.14206865"

    defaultConfig {
        applicationId = "com.mkhokheli.pocketcodedweb"
        minSdk = 25
        targetSdk = 36
        versionCode = 32
        versionName = "3.9.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++17", "-fexceptions", "-frtti")
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
            version = "3.22.1"
        }
    }
    packaging {
        jniLibs {
            // Git is launched by ProcessBuilder and therefore needs a real executable
            // filesystem path instead of mmap-only modern JNI packaging.
            useLegacyPackaging = true
        }
        resources {
            excludes += setOf(
                "plugin.properties",
                "OSGI-INF/l10n/plugin.properties",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

dependencies {
    implementation(libs.play.billing)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.googleid)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.jgit)
    implementation(libs.jgit.ssh.apache) {
        exclude(group = "org.apache.sshd", module = "sshd-osgi")
    }
    implementation(libs.sshd.sftp) {
        exclude(group = "org.apache.sshd", module = "sshd-common")
        exclude(group = "org.apache.sshd", module = "sshd-core")
    }
    implementation(libs.eddsa)
    implementation(files("libs/sshd-osgi-2.17.1-android.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.firebase.appcheck.debug)
}

// Check the merged libraries, including native dependencies, before packaging.
// Compiler flags cannot repair an incompatible prebuilt libnode.so.
listOf("Debug", "Release").forEach { variant ->
    val variantDirectory = variant.lowercase()
    val verifyAlignment = tasks.register("verify${variant}NativeAlignment") {
        dependsOn("merge${variant}NativeLibs")
        val libraries = layout.buildDirectory.dir(
            "intermediates/merged_native_libs/$variantDirectory/merge${variant}NativeLibs/out/lib"
        )
        inputs.dir(libraries)
        doLast {
            val nativeFiles = libraries.get().asFile.walkTopDown()
                .filter { it.isFile && it.extension == "so" }.toList()
            check(nativeFiles.isNotEmpty()) { "No merged native libraries found to validate" }
            nativeFiles.forEach { library ->
                RandomAccessFile(library, "r").use { input ->
                    val header = ByteArray(64)
                    input.readFully(header)
                    check(header.take(4) == listOf<Byte>(127, 69, 76, 70)) {
                        "Not an ELF library: $library"
                    }
                    if (header[4].toInt() == 2) {
                        check(header[5].toInt() == 1) { "Unsupported ELF byte order: $library" }
                        val elf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
                        val offset = elf.getLong(32)
                        val size = elf.getShort(54).toInt() and 0xffff
                        val count = elf.getShort(56).toInt() and 0xffff
                        check(size >= 56 && offset >= 0 && offset + size.toLong() * count <= input.length()) {
                            "Invalid ELF program headers: $library"
                        }
                        var loads = 0
                        repeat(count) { index ->
                            input.seek(offset + index.toLong() * size)
                            val bytes = ByteArray(56)
                            input.readFully(bytes)
                            val segment = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                            if (segment.getInt(0) == 1) {
                                loads++
                                val alignment = segment.getLong(48)
                                check(alignment >= 16384 && (alignment and (alignment - 1)) == 0L &&
                                    segment.getLong(8) % 16384 == segment.getLong(16) % 16384) {
                                    "$library has a LOAD segment incompatible with 16 KB pages. " +
                                        "Rebuild or replace this native library before publishing."
                                }
                            }
                        }
                        check(loads > 0) { "No ELF LOAD segments: $library" }
                    }
                }
            }
            logger.lifecycle("Native libraries passed 16 KB ELF alignment validation ($variant)")
        }
    }
    tasks.matching {
        it.name == "package$variant" || it.name == "package${variant}Bundle"
    }.configureEach { dependsOn(verifyAlignment) }
}
