package com.mkhokheli.pocketcodedweb

import android.content.Context
import android.system.Os
import java.io.File

/** Installs stable links to the read-only, APK-packaged Git executable. */
internal object NativeGitRuntime {
    const val VERSION = "2.55.0"
    private const val LIBRARY_NAME = "libpocketgit.so"
    @Volatile private var installedGit: File? = null
    @Volatile private var installedPrefix: File? = null

    fun install(context: Context): Result<File> = runCatching {
        val prefix = File(context.filesDir, "usr")
        val bin = File(prefix, "bin").apply { mkdirs() }
        val gitCore = File(prefix, "libexec/git-core").apply { mkdirs() }
        File(prefix, "share/git-core/templates").mkdirs()

        val packagedGit = File(context.applicationInfo.nativeLibraryDir, LIBRARY_NAME)
        require(packagedGit.isFile) { "Native Git is unavailable for this device ABI" }

        val git = File(bin, "git")
        replaceSymlink(git, packagedGit)
        replaceSymlink(File(gitCore, "git"), packagedGit)
        val marker = File(prefix, ".pocketcodedweb-git-version")
        if (!marker.isFile || marker.readText() != VERSION) marker.writeText(VERSION)
        installedPrefix = prefix
        installedGit = git
        git
    }

    fun executable(): File = installedGit ?: File(
        "/data/user/0/${BuildConfig.APPLICATION_ID}/files/usr/bin/git"
    )

    fun prefix(): File = installedPrefix ?: executable().parentFile!!.parentFile!!

    private fun replaceSymlink(link: File, target: File) {
        if (runCatching { link.canonicalFile == target.canonicalFile }.getOrDefault(false)) return
        if (link.exists() || runCatching { Os.readlink(link.absolutePath) }.isSuccess) {
            check(link.delete()) { "Could not replace ${link.absolutePath}" }
        }
        Os.symlink(target.absolutePath, link.absolutePath)
    }
}
