package com.mkhokheli.pocketcodedweb

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeGitCommandTest {
    @Test
    fun readOnlyCommandsDoNotForceWorkspaceRefresh() {
        listOf(
            listOf("status", "--short"), listOf("diff", "--cached"),
            listOf("log", "--oneline", "-100"), listOf("show", "HEAD:README.md"),
            listOf("branch", "--all"), listOf("tag", "--list"), listOf("fsck", "--full"),
        ).forEach { assertFalse("Expected read-only: $it", nativeGitMayChangeWorkspace(it)) }
    }

    @Test
    fun mutatingAndUnknownCommandsRefreshWorkspace() {
        listOf(
            listOf("add", "--all"), listOf("merge", "feature"), listOf("rebase", "main"),
            listOf("stash", "push"), listOf("reset", "--hard", "HEAD"),
            listOf("worktree", "add", "../review"), listOf("submodule", "update", "--init"),
            listOf("future-command"),
        ).forEach { assertTrue("Expected mutating: $it", nativeGitMayChangeWorkspace(it)) }
    }

    @Test
    fun emptyAndVersionQueriesAreReadOnly() {
        assertFalse(nativeGitMayChangeWorkspace(emptyList()))
        assertFalse(nativeGitMayChangeWorkspace(listOf("--version")))
        assertFalse(nativeGitMayChangeWorkspace(listOf("version")))
    }
}
