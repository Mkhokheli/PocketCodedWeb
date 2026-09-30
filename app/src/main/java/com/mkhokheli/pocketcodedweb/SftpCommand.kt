package com.mkhokheli.pocketcodedweb

import android.os.Build
import java.io.File
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier
import org.apache.sshd.common.NamedResource
import org.apache.sshd.common.config.keys.FilePasswordProvider
import org.apache.sshd.common.util.security.SecurityUtils
import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.client.SftpClientFactory

internal data class SftpConnectionOptions(
    val user: String,
    val host: String,
    val port: Int,
    val identityFile: File?,
    val recursive: Boolean,
    val command: String,
    val commandArgs: List<String>,
)

internal fun sftpHelp(): String = """
    Built-in SFTP (SSH key authentication)
    sftp [-P port] [-i identity] ls user@host [remote-dir]
    sftp [-P port] [-i identity] stat user@host <remote-path>
    sftp [-P port] [-i identity] get user@host <remote-file> [local-file]
    sftp [-P port] [-i identity] put user@host <local-file> [remote-file]
    sftp [-P port] [-i identity] get|put -r user@host <directory> [destination]
    sftp [-P port] [-i identity] mkdir|rm|rmdir user@host <remote-path>
    sftp [-P port] [-i identity] rename user@host <old-path> <new-path>
    sftp [-P port] [-i identity] chmod user@host <mode> <remote-path>

    Keys default to ~/.ssh/id_ed25519 or ~/.ssh/id_rsa. Unknown host keys are
    accepted once and saved to ~/.ssh/known_hosts; changed keys are rejected.
""".trimIndent()

internal fun parseSftpOptions(args: List<String>, root: File, working: File): Result<SftpConnectionOptions> = runCatching {
    if (args.isEmpty() || args.first() in setOf("help", "-h", "--help")) error(sftpHelp())
    var port = 22
    var identity: File? = null
    var recursive = false
    var index = 0
    while (index < args.size && args[index].startsWith("-")) {
        when (val option = args[index]) {
            "-P", "-p" -> port = args.getOrNull(++index)?.toIntOrNull()?.takeIf { it in 1..65535 }
                ?: error("sftp: invalid or missing port")
            "-i" -> {
                val path = args.getOrNull(++index) ?: error("sftp: missing identity after -i")
                identity = resolveSftpLocalPath(root, working, path) ?: error("sftp: $path: outside sandbox")
            }
            "-r" -> recursive = true
            else -> error("sftp: unsupported option $option\n${sftpHelp()}")
        }
        index++
    }
    val command = args.getOrNull(index++)?.lowercase() ?: error(sftpHelp())
    if (command !in setOf("ls", "stat", "get", "put", "mkdir", "rm", "rmdir", "rename", "chmod")) {
        error("sftp: unknown command $command\n${sftpHelp()}")
    }
    if (args.getOrNull(index) == "-r") { recursive = true; index++ }
    val endpoint = args.getOrNull(index++) ?: error("sftp: missing user@host")
    if (args.getOrNull(index) == "-r") { recursive = true; index++ }
    val match = Regex("^([^@\\s]+)@(?:\\[([^]]+)\\]|([^:\\s]+))(?::(\\d+))?$").matchEntire(endpoint)
        ?: error("sftp: connection must be written as user@host")
    if (match.groupValues[4].isNotBlank()) {
        port = match.groupValues[4].toIntOrNull()?.takeIf { it in 1..65535 }
            ?: error("sftp: invalid port")
    }
    SftpConnectionOptions(match.groupValues[1], match.groupValues[2].ifBlank { match.groupValues[3] }, port,
        identity, recursive, command, args.drop(index))
}

internal fun executeBuiltInSftpCommand(args: List<String>, root: File, working: File): WebShellResult {
    if (args.isEmpty() || args.first() in setOf("help", "-h", "--help")) return WebShellResult(output = sftpHelp())
    // SDK_INT is 0 in local JVM tests; real Android 7 devices must not reach
    // Apache SSHD's java.nio Path-based known-hosts implementation.
    if (Build.VERSION.SDK_INT != 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return WebShellResult(error = "sftp: Android 8 or newer is required")
    }
    val options = parseSftpOptions(args, root, working).getOrElse { return WebShellResult(error = it.message.orEmpty()) }
    val required = when (options.command) { "ls" -> 0; "rename", "chmod" -> 2; else -> 1 }
    if (options.commandArgs.size < required) return WebShellResult(error = "sftp ${options.command}: missing path\n${sftpHelp()}")

    val sshDir = File(root, ".ssh").apply { mkdirs() }
    val knownHosts = File(sshDir, "known_hosts").apply { if (!exists()) writeText("") }
    val identities = options.identityFile?.let(::listOf) ?: listOf("id_ed25519", "id_rsa", "id_ecdsa", "id_dsa")
        .map { File(sshDir, it) }.filter { it.isFile && it.length() > 0L }
    if (identities.isEmpty()) return WebShellResult(error = "sftp: no SSH key found in ~/.ssh. Import id_ed25519 or id_rsa, or use -i <identity-file>.")
    identities.firstOrNull { !it.isFile }?.let { return WebShellResult(error = "sftp: ${it.name}: identity file not found") }

    // Apache SSHD reads user.home during static initialization; Android does not
    // define one, so bind it to this project's sandbox before creating the client.
    System.setProperty("user.home", root.canonicalPath)
    val client = SshClient.setUpDefaultClient()
    client.serverKeyVerifier = KnownHostsServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE, knownHosts.toPath())
    return try {
        client.start()
        client.connect(options.user, options.host, options.port).verify(15_000L).session.use { session ->
            identities.forEach { file ->
                file.inputStream().use { input ->
                    SecurityUtils.loadKeyPairIdentities(session, NamedResource.ofName(file.name), input, FilePasswordProvider.EMPTY)
                        .forEach(session::addPublicKeyIdentity)
                }
            }
            session.auth().verify(15_000L)
            SftpClientFactory.instance().createSftpClient(session).use { executeSftpOperation(it, options, root, working) }
        }
    } catch (error: Exception) {
        val message = generateSequence(error as Throwable?) { it.cause }.mapNotNull { it.message }
            .firstOrNull { it.isNotBlank() } ?: error.javaClass.simpleName
        WebShellResult(error = "sftp: $message")
    } finally { runCatching { client.stop() } }
}

private fun executeSftpOperation(sftp: SftpClient, o: SftpConnectionOptions, root: File, working: File): WebShellResult {
    return when (o.command) {
    "ls" -> WebShellResult(output = sftp.readDir(o.commandArgs.firstOrNull() ?: ".")
        .filterNot { it.filename in setOf(".", "..") }.joinToString("\n") { it.longFilename.ifBlank { it.filename } })
    "stat" -> { val path = o.commandArgs[0]; val a = sftp.stat(path); val kind = when { a.isDirectory -> "directory"; a.isSymbolicLink -> "symbolic link"; else -> "file" }; WebShellResult(output = "$path\nType: $kind\nSize: ${a.size}") }
    "get" -> {
        val remote = o.commandArgs[0]
        val localText = o.commandArgs.getOrNull(1) ?: remote.substringAfterLast('/').ifBlank { "download" }
        val local = resolveSftpLocalPath(root, working, localText) ?: return WebShellResult(error = "sftp get: $localText: outside sandbox")
        if (o.recursive) downloadDirectory(sftp, remote, local) else downloadFile(sftp, remote, local)
        WebShellResult(output = "Downloaded $remote to ${local.relativeTo(root).invariantSeparatorsPath}", workspaceChanged = true)
    }
    "put" -> {
        val localText = o.commandArgs[0]
        val local = resolveSftpLocalPath(root, working, localText) ?: return WebShellResult(error = "sftp put: $localText: outside sandbox")
        val remote = o.commandArgs.getOrNull(1) ?: local.name
        if (o.recursive) { if (!local.isDirectory) return WebShellResult(error = "sftp put -r: $localText: not a directory"); uploadDirectory(sftp, local, remote) }
        else { if (!local.isFile) return WebShellResult(error = "sftp put: $localText: no such file"); uploadFile(sftp, local, remote) }
        WebShellResult(output = "Uploaded ${local.name} to $remote")
    }
    "mkdir" -> { sftp.mkdir(o.commandArgs[0]); WebShellResult(output = "Created ${o.commandArgs[0]}") }
    "rm" -> { sftp.remove(o.commandArgs[0]); WebShellResult(output = "Removed ${o.commandArgs[0]}") }
    "rmdir" -> { sftp.rmdir(o.commandArgs[0]); WebShellResult(output = "Removed ${o.commandArgs[0]}") }
    "rename" -> { sftp.rename(o.commandArgs[0], o.commandArgs[1]); WebShellResult(output = "Renamed ${o.commandArgs[0]} to ${o.commandArgs[1]}") }
    "chmod" -> { val mode = o.commandArgs[0].toIntOrNull(8)?.takeIf { it in 0..0x1ff } ?: return WebShellResult(error = "sftp chmod: invalid mode ${o.commandArgs[0]} (use 000-777)"); sftp.setStat(o.commandArgs[1], SftpClient.Attributes().apply { permissions = mode }); WebShellResult(output = "Changed ${o.commandArgs[1]} to ${o.commandArgs[0]}") }
        else -> WebShellResult(error = "sftp: unsupported command")
    }
}

private fun downloadFile(sftp: SftpClient, remote: String, local: File) {
    local.parentFile?.mkdirs(); val partial = File(local.parentFile, ".${local.name}.sftp-part")
    try { sftp.read(remote).use { input -> partial.outputStream().use(input::copyTo) }; if (local.exists() && !local.delete()) error("could not replace ${local.name}"); if (!partial.renameTo(local)) error("could not finish ${local.name}") }
    finally { if (partial.exists()) partial.delete() }
}

private fun downloadDirectory(sftp: SftpClient, remote: String, local: File) {
    if (!local.exists() && !local.mkdirs()) error("could not create ${local.name}")
    if (!local.isDirectory) error("${local.name}: not a directory")
    sftp.readDir(remote).filterNot { it.filename in setOf(".", "..") }.forEach { entry ->
        val remoteChild = remoteChild(remote, entry.filename); val localChild = File(local, entry.filename)
        if (entry.attributes.isDirectory) downloadDirectory(sftp, remoteChild, localChild) else downloadFile(sftp, remoteChild, localChild)
    }
}

private fun uploadFile(sftp: SftpClient, local: File, remote: String) = local.inputStream().use { input -> sftp.write(remote).use(input::copyTo) }
private fun uploadDirectory(sftp: SftpClient, local: File, remote: String) {
    runCatching { sftp.mkdir(remote) }.onFailure { error -> if (runCatching { !sftp.stat(remote).isDirectory }.getOrDefault(true)) throw error }
    local.listFiles().orEmpty().forEach { child -> val target = remoteChild(remote, child.name); if (child.isDirectory) uploadDirectory(sftp, child, target) else uploadFile(sftp, child, target) }
}
private fun remoteChild(parent: String, child: String) = if (parent.endsWith('/')) "$parent$child" else "$parent/$child"

private fun resolveSftpLocalPath(root: File, working: File, text: String): File? {
    val expanded = if (text == "~") root.path else if (text.startsWith("~/")) File(root, text.drop(2)).path else text
    val candidate = (if (File(expanded).isAbsolute) File(expanded) else File(working, expanded)).canonicalFile
    val canonicalRoot = root.canonicalFile
    return candidate.takeIf { it == canonicalRoot || it.path.startsWith(canonicalRoot.path + File.separator) }
}
