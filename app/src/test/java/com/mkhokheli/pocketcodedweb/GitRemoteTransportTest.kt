package com.mkhokheli.pocketcodedweb

import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GitRemoteTransportTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test
    fun clonePushAndPullWorkThroughJGitTransport() {
        val root = temporary.newFolder("workspace")
        val remote = temporary.newFolder("remote.git")
        Git.init().setBare(true).setDirectory(remote).call().close()

        val seed = temporary.newFolder("seed")
        Git.init().setInitialBranch("main").setDirectory(seed).call().use { git ->
            File(seed, "index.html").writeText("one\n")
            git.add().addFilepattern(".").call()
            git.commit().setMessage("seed").setAuthor("Test", "test@example.invalid").call()
            git.remoteAdd().setName("origin").setUri(URIish(remote.toURI().toString())).call()
            git.push().setRemote("origin").setPushAll().call()
        }

        val clone = executeGitRemoteCommand(
            listOf("clone", "--branch", "main", remote.toURI().toString(), "clone"), root, root,
        )
        assertTrue(clone.error, clone.error.isBlank())
        val checkout = File(root, "clone")
        assertTrue(File(checkout, "index.html").isFile)

        Git.open(checkout).use { git ->
            File(checkout, "index.html").appendText("two\n")
            git.add().addFilepattern(".").call()
            git.commit().setMessage("update").setAuthor("Test", "test@example.invalid").call()
        }
        val push = executeGitRemoteCommand(listOf("push", "origin", "main"), root, checkout)
        assertTrue(push.error, push.error.isBlank())

        val verification = temporary.newFolder("verification")
        Git.cloneRepository().setURI(remote.toURI().toString()).setBranch("main").setDirectory(verification).call().close()
        assertTrue(File(verification, "index.html").readText().contains("two"))
    }

    @Test
    fun cloneCannotEscapeProjectSandbox() {
        val root = temporary.newFolder("sandbox")
        val result = executeGitRemoteCommand(listOf("clone", "file:///missing.git", "../escape"), root, root)
        assertFalse(result.error.isBlank())
        assertFalse(File(root.parentFile, "escape").exists())
    }
}
