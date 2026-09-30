package com.mkhokheli.pocketcodedweb

import android.os.Build
import java.io.File
import java.net.URI
import java.net.URLDecoder
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder

/** Android-compatible network transport for commands omitted from the lean native binary. */
internal fun executeGitRemoteCommand(args: List<String>, root: File, working: File): WebShellResult = runCatching {
    configureGitTransport(root)
    when (args.firstOrNull()?.lowercase()) {
        "clone" -> gitRemoteClone(args.drop(1), root, working)
        "fetch" -> withRemoteRepository(working) { git ->
            val remote = args.drop(1).firstOrNull { !it.startsWith("-") } ?: "origin"
            val command = git.fetch().setRemote(remote).setTimeout(120)
            credentialsFor(git.repository.config.getString("remote", remote, "url"))
                ?.let(command::setCredentialsProvider)
            val result = command.call()
            WebShellResult(output = result.messages.trim().ifBlank { "Fetch completed." })
        }
        "pull" -> withRemoteRepository(working) { git ->
            val positional = args.drop(1).filterNot { it.startsWith("-") }
            val remote = positional.getOrNull(0)
            val branch = positional.getOrNull(1)
            val command = git.pull().setTimeout(120)
            remote?.let(command::setRemote)
            branch?.let(command::setRemoteBranchName)
            credentialsFor(remoteUrl(git, remote))?.let(command::setCredentialsProvider)
            val result = command.call()
            val details = buildList {
                add(if (result.isSuccessful) "Pull completed successfully." else "Pull completed with issues.")
                result.mergeResult?.mergeStatus?.let { add("Merge: $it") }
                result.rebaseResult?.status?.let { add("Rebase: $it") }
                result.fetchResult?.messages?.trim()?.takeIf(String::isNotBlank)?.let(::add)
            }
            WebShellResult(output = details.joinToString("\n"), workspaceChanged = true)
        }
        "push" -> withRemoteRepository(working) { git ->
            val positional = args.drop(1).filterNot { it.startsWith("-") }
            val remote = positional.getOrNull(0) ?: "origin"
            val branch = positional.getOrNull(1) ?: git.repository.branch
            val command = git.push().setRemote(remote).setTimeout(120)
            if (branch.isNotBlank() && branch != "HEAD") {
                command.setRefSpecs(RefSpec("refs/heads/$branch:refs/heads/$branch"))
            }
            credentialsFor(remoteUrl(git, remote))?.let(command::setCredentialsProvider)
            val lines = command.call().flatMap { result ->
                buildList {
                    result.messages?.trim()?.takeIf(String::isNotBlank)?.let(::add)
                    result.remoteUpdates.forEach { update ->
                        add("${update.remoteName}: ${update.status}${update.message?.let { ": $it" }.orEmpty()}")
                    }
                }
            }
            WebShellResult(output = lines.joinToString("\n").ifBlank { "Push completed." })
        }
        else -> WebShellResult(error = "git: unsupported remote command")
    }
}.getOrElse { error ->
    WebShellResult(error = "git: ${safeGitRemoteError(error)}")
}

private fun gitRemoteClone(args: List<String>, root: File, working: File): WebShellResult {
    var branch: String? = null
    var depth: Int? = null
    val positional = ArrayList<String>()
    var index = 0
    while (index < args.size) {
        when (val arg = args[index]) {
            "-b", "--branch" -> { branch = args.getOrNull(index + 1) ?: error("clone: missing branch"); index += 2 }
            "--depth" -> { depth = args.getOrNull(index + 1)?.toIntOrNull() ?: error("clone: invalid depth"); index += 2 }
            "--single-branch" -> index++
            else -> if (arg.startsWith("--branch=")) { branch = arg.substringAfter('='); index++ }
            else if (arg.startsWith("--depth=")) { depth = arg.substringAfter('=').toIntOrNull() ?: error("clone: invalid depth"); index++ }
            else if (arg.startsWith("-")) error("clone: unsupported option $arg")
            else { positional += arg; index++ }
        }
    }
    val url = positional.getOrNull(0) ?: error("clone: missing repository URL")
    if (isSshUrl(url) && Build.VERSION.SDK_INT in 1 until Build.VERSION_CODES.O) {
        error("SSH Git transport requires Android 8 or newer")
    }
    val defaultName = url.trimEnd('/').substringAfterLast('/').substringAfterLast(':').removeSuffix(".git").ifBlank { "repository" }
    val target = File(working, positional.getOrNull(1) ?: defaultName).canonicalFile
    require(isInside(root, target)) { "clone target is outside the project" }
    require(!target.exists() || target.listFiles().orEmpty().isEmpty()) { "clone target is not empty" }
    val command = Git.cloneRepository().setURI(url).setDirectory(target).setTimeout(120)
    branch?.let(command::setBranch)
    depth?.let(command::setDepth)
    credentialsFor(url)?.let(command::setCredentialsProvider)
    command.call().close()
    return WebShellResult(output = "Cloned ${redactRemote(url)} into ${target.name}", workspaceChanged = true)
}

private fun <T> withRemoteRepository(working: File, block: (Git) -> T): T {
    val builder = FileRepositoryBuilder().findGitDir(working)
    require(builder.gitDir != null) { "not a Git repository" }
    return Git(builder.build()).use(block)
}

private fun configureGitTransport(root: File) {
    val home = root.canonicalFile
    System.setProperty("user.home", home.absolutePath)
    val ssh = File(home, ".ssh").apply { mkdirs() }
    File(ssh, "known_hosts").apply { if (!exists()) writeText("") }
    File(ssh, "config").apply {
        if (!exists()) writeText(
            """
            Host *
                StrictHostKeyChecking accept-new
                UserKnownHostsFile ~/.ssh/known_hosts
                IdentityFile ~/.ssh/id_ed25519
                IdentityFile ~/.ssh/id_rsa
            """.trimIndent() + "\n"
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        SshSessionFactory.setInstance(
            SshdSessionFactoryBuilder().setHomeDirectory(home).setSshDirectory(ssh).setConnectorFactory(null).build(null)
        )
    }
}

private fun remoteUrl(git: Git, requested: String?): String? {
    val remote = requested ?: git.repository.config.getString("branch", git.repository.branch, "remote") ?: "origin"
    return git.repository.config.getString("remote", remote, "url")
}

private fun credentialsFor(url: String?): CredentialsProvider? {
    if (url == null || !url.startsWith("http", true)) return null
    val info = runCatching { URI(url).rawUserInfo }.getOrNull() ?: return null
    val parts = info.split(':', limit = 2)
    return UsernamePasswordCredentialsProvider(
        URLDecoder.decode(parts[0], "UTF-8"),
        URLDecoder.decode(parts.getOrElse(1) { "" }, "UTF-8"),
    )
}

private fun isSshUrl(url: String): Boolean = url.startsWith("ssh://", true) ||
    Regex("^[A-Za-z0-9._%+-]+@[^:]+:.+").matches(url)

private fun redactRemote(url: String): String = runCatching {
    val uri = URI(url)
    if (uri.rawUserInfo == null) url else URI(uri.scheme, null, uri.host, uri.port, uri.path, uri.query, uri.fragment).toString()
}.getOrDefault(url.replace(Regex("//[^/@]+@"), "//***@"))

private fun safeGitRemoteError(error: Throwable): String {
    val message = generateSequence(error) { it.cause }.mapNotNull { it.message }.firstOrNull { it.isNotBlank() }
        ?: error.javaClass.simpleName
    return redactRemote(message).take(2_000)
}

private fun isInside(root: File, child: File): Boolean {
    val base = root.canonicalFile.path
    val path = child.canonicalFile.path
    return path == base || path.startsWith(base + File.separator)
}
