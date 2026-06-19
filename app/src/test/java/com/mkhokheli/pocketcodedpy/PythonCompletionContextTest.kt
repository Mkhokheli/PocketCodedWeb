package com.mkhokheli.pocketcodedpy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PythonCompletionContextTest {
    @Test
    fun completionIsSuppressedInsidePythonStrings() {
        assertFalse(isPythonCompletionPosition("name = \"pri\"", 10))
        assertFalse(isPythonCompletionPosition("name = 'pri'", 10))
        assertFalse(isPythonCompletionPosition("text = \"\"\"pri\"\"\"", 13))
        assertFalse(isPythonCompletionPosition("text = 'it\\\'s pri'", 15))
    }

    @Test
    fun completionReturnsAfterAStringCloses() {
        val source = "name = \"Ada\"\npri"

        assertTrue(isPythonCompletionPosition(source, source.length))
    }

    @Test
    fun completionIsSuppressedInCommentsButReturnsOnTheNextLine() {
        val comment = "# call pri"
        val nextLine = "$comment\npri"

        assertFalse(isPythonCompletionPosition(comment, comment.length))
        assertTrue(isPythonCompletionPosition(nextLine, nextLine.length))
    }
}
