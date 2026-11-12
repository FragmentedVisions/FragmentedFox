/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.search.middleware

import mozilla.components.browser.state.search.SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class ReplaceBingWithEcosiaTest {
    private fun engine(
        id: String,
        type: SearchEngine.Type = SearchEngine.Type.BUNDLED,
        isOptional: Boolean = false,
    ) = SearchEngine(
        id = id,
        name = id,
        icon = mock(android.graphics.Bitmap::class.java),
        type = type,
        isOptional = isOptional,
        resultUrls = listOf("https://example.com/?q={searchTerms}"),
    )

    @Test
    fun `GIVEN Bing in the list WHEN replacing THEN Ecosia takes its place`() {
        val list = listOf(engine("google"), engine("bing"), engine("ddg"))

        val result = list.replaceBingWithEcosia(engine("ecosia", SearchEngine.Type.BUNDLED_ADDITIONAL, true))

        assertEquals(listOf("google", "ecosia", "ddg"), result.map { it.id })
        assertFalse(result[1].isOptional)
        assertEquals(SearchEngine.Type.BUNDLED, result[1].type)
    }

    @Test
    fun `GIVEN several Bing engines WHEN replacing THEN none are left and Ecosia appears once`() {
        val list = listOf(engine("bing"), engine("google"), engine("bing-vivo"), engine("ecosia"))

        val result = list.replaceBingWithEcosia(engine("ecosia"))

        assertEquals(listOf("ecosia", "google"), result.map { it.id })
    }

    @Test
    fun `GIVEN no Bing in the list WHEN replacing THEN Ecosia is added at the end`() {
        val result = listOf(engine("google"), engine("ddg")).replaceBingWithEcosia(engine("ecosia"))

        assertEquals(listOf("google", "ddg", "ecosia"), result.map { it.id })
    }

    @Test
    fun `GIVEN Ecosia could not be loaded WHEN replacing THEN Bing is still removed`() {
        val result = listOf(engine("google"), engine("bing")).replaceBingWithEcosia(null)

        assertEquals(listOf("google"), result.map { it.id })
        assertTrue(result.none { it.id.startsWith("bing") })
    }
}
