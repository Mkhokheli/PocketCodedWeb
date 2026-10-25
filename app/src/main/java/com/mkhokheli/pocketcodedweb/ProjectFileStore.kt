package com.mkhokheli.pocketcodedweb

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal data class PendingFileEdit internal constructor(
    val filePath: String,
    val content: String,
    internal val sequence: Long,
)

internal data class FileRevision(
    val id: String,
    val relativePath: String,
    val savedAt: Long,
    val sizeBytes: Int,
)

internal data class RecoveredDraft(
    val filePath: String,
    val relativePath: String,
    val savedAt: Long,
    val sizeBytes: Int,
)

/**
 * Atomic editor writes, persistent recovery drafts, and bounded previous versions.
 *
 * Call [prepareEdit] on each editor change before dispatching work to IO. Staging or
 * saving an older edit is then harmless, including when a cancelled coroutine has
 * already entered a blocking disk operation. No operation ever deletes the live
 * file in order to replace it. Failed writes leave both the live file and draft.
 */
internal class ProjectFileStore(
    projectDirectory: File,
    private val maxRevisions: Int = 40,
    private val historyBytesPerFile: Long = 8L * 1024 * 1024,
    private val maxHistoryBytes: Long = 64L * 1024 * 1024,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    val projectDirectory: File = projectDirectory.canonicalFile
    private val metadataDirectory = File(this.projectDirectory, ".pocketcodedweb/history")
    private val historyDirectory = File(metadataDirectory, "history")
    private val recoveryDirectory = File(metadataDirectory, "recovery")
    private val state = states.getOrPut(this.projectDirectory.path) { StoreState() }

    init {
        require(maxRevisions > 0)
        require(historyBytesPerFile > 0 && maxHistoryBytes > 0)
    }

    fun prepareEdit(filePath: String, content: String, expectedContent: String? = null): PendingFileEdit {
        val target = targetFile(filePath)
        // This runs while typing. Never wait for a file write/history scan or hash a
        // large document while holding the metadata monitor.
        return synchronized(state) {
            if (expectedContent != null && target.path !in state.expectedContents) {
                state.expectedContents[target.path] = expectedContent
            }
            PendingFileEdit(target.path, content, ++state.sequence).also {
                state.latestEdits[target.path] = it.sequence
            }
        }
    }

    fun stageDraft(edit: PendingFileEdit): Result<Unit> = synchronized(state.ioLock) {
        runCatching {
            val target = targetFile(edit.filePath)
            if (!isLatest(edit)) return@runCatching
            if (target.path in state.protectedDrafts) throw IOException("Resolve the recovery copy for ${target.name} before saving new edits.")
            val bytes = contentBytes(edit.content)
            if (target.isFile && target.length() == bytes.size.toLong() && readBoundedFile(target).contentEquals(bytes)) {
                deleteDraft(target)
            } else {
                writeSnapshot(draftFile(target), relativePath(target), clock(), bytes)
            }
        }
    }

    fun save(filePath: String, content: String): Result<Unit> = runCatching {
        save(prepareEdit(filePath, content)).getOrThrow()
    }

    fun save(edit: PendingFileEdit): Result<Unit> = synchronized(state.ioLock) {
        runCatching {
            val target = targetFile(edit.filePath)
            if (!isLatest(edit)) return@runCatching
            if (target.path in state.protectedDrafts) throw IOException("Resolve the recovery copy for ${target.name} before saving new edits.")
            val bytes = contentBytes(edit.content)
            if (target.exists() && !target.isFile) throw IOException("${target.name} is not a regular file.")
            // Even conflict/size failures retain the editor contents for recovery.
            writeSnapshot(draftFile(target), relativePath(target), clock(), bytes)
            if (!isLatest(edit)) return@runCatching
            val previous = target.takeIf(File::isFile)?.let(::readBoundedFile)
            if (previous != null && previous.contentEquals(bytes)) {
                deleteDraft(target)
                completeSave(edit)
                return@runCatching
            }
            synchronized(state) { state.expectedContents[target.path] }?.let { expected ->
                if (previous == null || !previous.contentEquals(expected.toByteArray(Charsets.UTF_8))) {
                    throw IOException("${target.name} changed outside the editor. Your edits were kept in a recovery copy. Reopen the project to choose which version to keep.")
                }
            }
            if (previous != null) retainPreviousVersion(target, previous)
            durableAtomicWrite(target, bytes)
            completeSave(edit)
            deleteDraft(target)
            pruneHistory()
        }
    }

    /** Previous saved contents, newest first. The live file is never a revision. */
    fun history(filePath: String): List<FileRevision> = synchronized(state.ioLock) {
        val target = targetFile(filePath)
        revisionFiles(target).mapNotNull { file ->
            readHeader(file)?.takeIf { it.path == relativePath(target) }?.let {
                FileRevision(file.name, it.path, it.savedAt, it.sizeBytes)
            }
        }.sortedByDescending { it.id }
    }

    fun readRevision(filePath: String, revisionId: String): Result<String> = synchronized(state.ioLock) {
        runCatching {
            val target = targetFile(filePath)
            if (!REVISION_ID.matches(revisionId)) throw IOException("Invalid history entry.")
            val snapshot = readSnapshot(File(historyFolder(target), revisionId))
            if (snapshot.header.path != relativePath(target)) throw IOException("This history entry belongs to another file.")
            snapshot.content.toString(Charsets.UTF_8)
        }
    }

    /** Restoring itself goes through save, so the current version can be restored again. */
    fun restore(filePath: String, revisionId: String): Result<String> = synchronized(state.ioLock) {
        readRevision(filePath, revisionId).mapCatching { content ->
            val target = targetFile(filePath)
            synchronized(state) { state.expectedContents.remove(target.path) }
            save(filePath, content).getOrThrow()
            content
        }
    }

    fun recoveryDrafts(): List<RecoveredDraft> = synchronized(state.ioLock) {
        recoveryDirectory.listFiles().orEmpty().filter { it.extension == "draft" }.mapNotNull { file ->
            runCatching {
                val snapshot = readSnapshot(file)
                val target = targetFile(File(projectDirectory, snapshot.header.path).path)
                if (file.name != draftFile(target).name) return@runCatching null
                if (target.isFile && target.length() == snapshot.content.size.toLong() && readBoundedFile(target).contentEquals(snapshot.content)) {
                    file.delete()
                    return@runCatching null
                }
                state.protectedDrafts.add(target.path)
                RecoveredDraft(target.path, snapshot.header.path, snapshot.header.savedAt, snapshot.header.sizeBytes)
            }.getOrNull()
        }.sortedByDescending { it.savedAt }
    }

    fun readDraft(filePath: String): Result<String> = synchronized(state.ioLock) {
        runCatching {
            val target = targetFile(filePath)
            val snapshot = readSnapshot(draftFile(target))
            if (snapshot.header.path != relativePath(target)) throw IOException("This recovery copy belongs to another file.")
            snapshot.content.toString(Charsets.UTF_8)
        }
    }

    fun recoverDraft(filePath: String): Result<String> = synchronized(state.ioLock) {
        runCatching {
            val target = targetFile(filePath)
            val snapshot = readSnapshot(draftFile(target))
            if (snapshot.header.path != relativePath(target)) throw IOException("This recovery copy belongs to another file.")
            val content = snapshot.content.toString(Charsets.UTF_8)
            state.protectedDrafts.remove(target.path)
            synchronized(state) { state.expectedContents.remove(target.path) }
            try { save(target.path, content).getOrThrow() }
            catch (error: Throwable) { state.protectedDrafts.add(target.path); throw error }
            content
        }
    }

    fun discardDraft(filePath: String): Result<Unit> = synchronized(state.ioLock) {
        invalidatePendingEdit(filePath)
    }

    /** Waits for any active write and prevents cancelled disk jobs from recreating a deleted/renamed file. */
    fun invalidatePendingEdit(filePath: String): Result<Unit> = synchronized(state.ioLock) {
        runCatching {
            val target = targetFile(filePath)
            try {
                state.protectedDrafts.remove(target.path)
                deleteDraft(target)
            } finally {
                synchronized(state) {
                    state.latestEdits[target.path] = ++state.sequence
                    state.expectedContents.remove(target.path)
                }
            }
        }
    }

    /** The directory may be the project root, but must never resolve outside it. */
    fun invalidateUnder(directoryPath: String): Result<Unit> = synchronized(state.ioLock) {
        runCatching {
            val directory = File(directoryPath).canonicalFile
            if (directory != projectDirectory && !directory.path.startsWith(projectDirectory.path + File.separator)) {
                throw IOException("Select a folder inside this project.")
            }
            fun isUnder(path: String) = path == directory.path || path.startsWith(directory.path + File.separator)
            try {
                recoveryDirectory.listFiles().orEmpty().filter { it.extension == "draft" }.forEach { draft ->
                    val snapshot = runCatching { readSnapshot(draft) }.getOrNull() ?: return@forEach
                    val target = runCatching { targetFile(File(projectDirectory, snapshot.header.path).path) }.getOrNull() ?: return@forEach
                    if (isUnder(target.path)) deleteDraft(target)
                }
            } finally {
                synchronized(state) {
                    state.latestEdits.keys.filter(::isUnder).forEach { path ->
                        state.latestEdits[path] = ++state.sequence
                        state.expectedContents.remove(path)
                    }
                }
            }
        }
    }

    private fun isLatest(edit: PendingFileEdit): Boolean = synchronized(state) {
        state.latestEdits[edit.filePath] == edit.sequence
    }

    private fun completeSave(edit: PendingFileEdit) = synchronized(state) {
        if (state.latestEdits[edit.filePath] == edit.sequence) {
            state.expectedContents.remove(edit.filePath)
        } else {
            // Typing can continue during fsync. A newer pending edit must compare
            // against the contents this in-flight save just committed.
            state.expectedContents[edit.filePath] = edit.content
        }
    }

    private fun targetFile(path: String): File {
        val target = File(path).canonicalFile
        val prefix = projectDirectory.path + File.separator
        if (!target.path.startsWith(prefix) || target.path.startsWith(File(projectDirectory, ".pocketcodedweb").path + File.separator) ||
            target.path.startsWith(metadataDirectory.path + File.separator) || target == metadataDirectory) {
            throw IOException("Select a file inside this project.")
        }
        if (target.isDirectory) throw IOException("${target.name} is a folder.")
        return target
    }

    private fun contentBytes(content: String): ByteArray = content.toByteArray(Charsets.UTF_8).also {
        if (it.size > MAX_DOCUMENT_BYTES) throw IOException("This file is too large to save from the editor.")
    }

    private fun readBoundedFile(file: File): ByteArray {
        if (file.length() > MAX_DOCUMENT_BYTES) throw IOException("The original file is too large to preserve safely.")
        return file.inputStream().use { input ->
            val output = ByteArrayOutputStream(file.length().toInt().coerceAtLeast(32))
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (output.size() + count > MAX_DOCUMENT_BYTES) throw IOException("The original file is too large to preserve safely.")
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    }

    private fun relativePath(target: File): String = target.relativeTo(projectDirectory).invariantSeparatorsPath
    private fun key(target: File): String = digest(relativePath(target).toByteArray(Charsets.UTF_8)).toHex()
    private fun draftFile(target: File) = File(recoveryDirectory, "${key(target)}.draft")
    private fun historyFolder(target: File) = File(historyDirectory, key(target))
    private fun revisionFiles(target: File) = historyFolder(target).listFiles().orEmpty().filter { REVISION_ID.matches(it.name) }

    private fun deleteDraft(target: File) {
        val file = draftFile(target)
        if (file.exists() && !file.delete()) throw IOException("The recovery copy could not be cleared.")
    }

    private fun retainPreviousVersion(target: File, previous: ByteArray) {
        if (previous.size > MAX_DOCUMENT_BYTES) throw IOException("The original file is too large to preserve safely.")
        val newest = history(target.path).firstOrNull()
        if (newest != null) {
            val latestSnapshot = runCatching { readSnapshot(File(historyFolder(target), newest.id)) }.getOrNull()
            if (latestSnapshot?.content?.contentEquals(previous) == true) return
        }
        val previousTimestamp = revisionFiles(target).mapNotNull { it.name.substringBefore('-').toLongOrNull() }.maxOrNull() ?: 0L
        val timestamp = maxOf(clock(), state.lastHistoryTimestamp + 1, previousTimestamp + 1)
        state.lastHistoryTimestamp = timestamp
        val name = "${timestamp.toString().padStart(19, '0')}-${UUID.randomUUID()}.rev"
        writeSnapshot(File(historyFolder(target), name), relativePath(target), clock(), previous)
        val revisions = revisionFiles(target).sortedByDescending { it.name }
        var used = 0L
        revisions.forEachIndexed { index, file ->
            used += file.length()
            // Always retain the most recent backup, even if it alone exceeds the quota.
            if (index > 0 && (index >= maxRevisions || used > historyBytesPerFile)) file.delete()
        }
    }

    private fun pruneHistory() {
        val revisions = historyDirectory.listFiles().orEmpty().flatMap { directory ->
            directory.listFiles().orEmpty().filter { REVISION_ID.matches(it.name) }
        }.sortedByDescending { it.name }
        var used = 0L
        revisions.forEach { file ->
            used += file.length()
            if (used > maxHistoryBytes) file.delete()
        }
    }

    private data class SnapshotHeader(val path: String, val savedAt: Long, val sizeBytes: Int)
    private data class Snapshot(val header: SnapshotHeader, val content: ByteArray)

    private fun writeSnapshot(file: File, path: String, savedAt: Long, content: ByteArray) {
        validateSnapshotPath(file)
        val bytes = ByteArrayOutputStream(content.size + 256).also { buffer ->
            DataOutputStream(buffer).use { output ->
                output.writeInt(SNAPSHOT_MAGIC)
                output.writeUTF(path)
                output.writeLong(savedAt)
                output.writeInt(content.size)
                output.write(content)
                output.write(digest(content))
            }
        }.toByteArray()
        durableAtomicWrite(file, bytes)
    }

    private fun readHeader(file: File): SnapshotHeader? = runCatching {
        validateSnapshotPath(file)
        DataInputStream(file.inputStream().buffered()).use(::readHeader)
    }.getOrNull()

    private fun readHeader(input: DataInputStream): SnapshotHeader {
        if (input.readInt() != SNAPSHOT_MAGIC) throw IOException("The saved copy has an invalid format.")
        val path = input.readUTF()
        val savedAt = input.readLong()
        val size = input.readInt()
        if (size !in 0..MAX_DOCUMENT_BYTES) throw IOException("The saved copy has an invalid size.")
        return SnapshotHeader(path, savedAt, size)
    }

    private fun readSnapshot(file: File): Snapshot {
        validateSnapshotPath(file)
        DataInputStream(file.inputStream().buffered()).use { input ->
            val header = readHeader(input)
            val content = ByteArray(header.sizeBytes)
            input.readFully(content)
            val checksum = ByteArray(32)
            input.readFully(checksum)
            if (!checksum.contentEquals(digest(content)) || input.read() != -1) throw IOException("The saved copy is damaged.")
            return Snapshot(header, content)
        }
    }

    private fun validateSnapshotPath(file: File) {
        val metadata = metadataDirectory.canonicalFile
        if (!metadata.path.startsWith(projectDirectory.path + File.separator) ||
            !file.canonicalFile.path.startsWith(metadata.path + File.separator)) {
            throw IOException("The local history folder points outside this project.")
        }
    }

    companion object {
        private const val SNAPSHOT_MAGIC = 0x50435031 // PCP1
        private const val MAX_DOCUMENT_BYTES = 8 * 1024 * 1024
        private val REVISION_ID = Regex("[0-9]+-[0-9a-f-]{36}\\.rev")
        private val states = ConcurrentHashMap<String, StoreState>()
        private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        private fun ByteArray.toHex() = joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    private class StoreState {
        // Disk operations may briefly take the metadata monitor; preparation
        // takes only the metadata monitor and never waits for ioLock.
        val ioLock = Any()
        val protectedDrafts = mutableSetOf<String>()
        var sequence = 0L
        var lastHistoryTimestamp = 0L
        val latestEdits = mutableMapOf<String, Long>()
        val expectedContents = mutableMapOf<String, String>()
    }
}

/** Android's same-filesystem rename atomically replaces an existing regular file. */
internal fun atomicWriteFile(target: File, content: String) = durableAtomicWrite(target, content.toByteArray(Charsets.UTF_8))

internal fun durableAtomicWrite(target: File, bytes: ByteArray) {
    val parent = target.parentFile ?: throw IOException("The destination folder is unavailable.")
    if (!parent.isDirectory && !parent.mkdirs()) throw IOException("Could not create ${parent.name}.")
    val temporary = File(parent, ".${target.name}.${UUID.randomUUID()}.saving")
    try {
        FileOutputStream(temporary).use { output ->
            output.write(bytes)
            output.flush()
            output.fd.sync()
        }
        if (target.isFile && target.canExecute() && !temporary.setExecutable(true, true)) {
            throw IOException("Could not preserve executable permission for ${target.name}.")
        }
        if (!temporary.renameTo(target) && !replaceAtomicallyOnWindows(temporary, target)) {
            throw IOException("Could not replace ${target.name}; the original file was kept.")
        }
    } finally {
        temporary.delete()
    }
}

/** File.renameTo replaces existing files on Android, while Windows requires NIO. */
private fun replaceAtomicallyOnWindows(source: File, target: File): Boolean {
    if (File.separatorChar != '\\') return false
    // Reflection keeps the API 26 java.nio.file classes out of Android 24/25 code
    // resolution. This branch is used only by desktop tooling and JVM tests.
    return runCatching {
        val pathType = Class.forName("java.nio.file.Path")
        val optionType = Class.forName("java.nio.file.CopyOption")
        val standardOptions = Class.forName("java.nio.file.StandardCopyOption").enumConstants
        val options = java.lang.reflect.Array.newInstance(optionType, 2)
        java.lang.reflect.Array.set(options, 0, standardOptions.first { (it as Enum<*>).name == "ATOMIC_MOVE" })
        java.lang.reflect.Array.set(options, 1, standardOptions.first { (it as Enum<*>).name == "REPLACE_EXISTING" })
        val toPath = File::class.java.getMethod("toPath")
        Class.forName("java.nio.file.Files").getMethod("move", pathType, pathType, options.javaClass)
            .invoke(null, toPath.invoke(source), toPath.invoke(target), options)
        true
    }.getOrDefault(false)
}
