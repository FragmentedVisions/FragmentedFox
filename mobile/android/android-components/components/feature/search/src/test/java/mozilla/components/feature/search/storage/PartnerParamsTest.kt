/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.search.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class PartnerParamsTest {
    @Test
    fun `GIVEN a Google URL WHEN stripping THEN the client and channel params are removed`() {
        assertEquals(
            "https://www.google.com/search?q={searchTerms}",
            PartnerParams.strip("https://www.google.com/search?client=firefox-b-m&q={searchTerms}&channel=ts"),
        )
    }

    @Test
    fun `GIVEN a Bing URL WHEN stripping THEN the pc and form params are removed`() {
        assertEquals(
            "https://www.bing.com/search?q={searchTerms}",
            PartnerParams.strip("https://www.bing.com/search?pc=MOZB&form=MOZMBA&q={searchTerms}"),
        )
    }

    @Test
    fun `GIVEN a DuckDuckGo URL WHEN stripping THEN the t param is removed`() {
        assertEquals(
            "https://duckduckgo.com/?q={searchTerms}",
            PartnerParams.strip("https://duckduckgo.com/?q={searchTerms}&t=fpas"),
        )
    }

    @Test
    fun `GIVEN only partner params WHEN stripping THEN no query is left`() {
        assertEquals("https://duckduckgo.com/", PartnerParams.strip("https://duckduckgo.com/?t=fpas"))
    }

    @Test
    fun `GIVEN a fragment WHEN stripping THEN the fragment is kept`() {
        assertEquals(
            "https://www.google.com/search?q=a#top",
            PartnerParams.strip("https://www.google.com/search?client=x&q=a#top"),
        )
    }

    @Test
    fun `GIVEN an engine without rules WHEN stripping THEN the URL is unchanged`() {
        val url = "https://example.com/search?client=mine&q={searchTerms}"
        assertEquals(url, PartnerParams.strip(url))
    }

    @Test
    fun `GIVEN a URL without a query WHEN stripping THEN the URL is unchanged`() {
        val url = "https://www.google.com/"
        assertEquals(url, PartnerParams.strip(url))
    }

    @Test
    fun `GIVEN an eBay URL WHEN stripping THEN the campaign params are removed but the site is kept`() {
        assertEquals(
            "https://www.ebay.com/sch/i.html?siteid=0&_nkw={searchTerms}",
            PartnerParams.strip(
                "https://www.ebay.com/sch/i.html?mkcid=1&mkrid=711-53200-19255-0&siteid=0&campid=5338791379" +
                    "&customid=&toolid=20004&mkevt=1&_nkw={searchTerms}",
            ),
        )
    }
}
