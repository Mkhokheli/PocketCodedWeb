package com.mkhokheli.pocketcodedweb

import java.io.File

/** One AI edit batch. Checks every expected result before restoring any file. */
internal class AiFileUndo private constructor(
    private val before: Map<File, ByteArray?>,
    private val after: Map<File, ByteArray?>,
) {
    val paths: List<String> get() = before.keys.map { it.absolutePath }

    fun undo(store: ProjectFileStore? = null) {
        after.forEach { (file, expected) ->
            check(file.canonicalFile == file.absoluteFile) { "${file.name} moved since the AI edit." }
            check(!file.isDirectory && same(read(file), expected)) {
                "${file.name} changed since the AI edit. Undo was stopped to preserve your newer changes."
            }
        }
        val restored = mutableListOf<File>()
        try {
            before.forEach { (file, content) ->
                restored += file
                if (store != null && content != null) store.save(file.path, content.toString(Charsets.UTF_8)).getOrThrow()
                else write(file, content)
            }
        } catch (error: Throwable) {
            restored.asReversed().forEach { file -> runCatching { write(file, after[file]) }.onFailure(error::addSuppressed) }
            throw error
        }
    }

    companion object {
        private const val MAX_UNDO_BYTES = 32L * 1024 * 1024
        fun capture(files: List<File>): Map<File, ByteArray?> {
            val targets = files.map { it.canonicalFile }.distinct()
            require(targets.none { it.isDirectory }) { "AI undo supports files, not folders." }
            require(targets.sumOf { it.length() } <= MAX_UNDO_BYTES) { "This AI edit exceeds the 32 MiB undo limit." }
            return targets.associateWith(::read)
        }
        fun completed(before: Map<File, ByteArray?>): AiFileUndo = AiFileUndo(before, capture(before.keys.toList()))
        private fun read(file: File): ByteArray? = if (file.exists()) file.readBytes() else null
        private fun same(left: ByteArray?, right: ByteArray?): Boolean =
            if (left == null || right == null) left == null && right == null else left.contentEquals(right)
        private fun write(file: File, bytes: ByteArray?) {
            if (bytes == null) check(!file.exists() || file.delete()) { "Could not remove ${file.name}." }
            else { file.parentFile?.mkdirs(); file.writeBytes(bytes) }
        }
    }
}
