/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.search.storage

/**
 * Removes the partner and attribution query parameters that search engine definitions add to
 * search result URLs, so that searches do not say they came from this browser.
 */
internal object PartnerParams {
    private val rules = listOf(
        Regex("""(^|\.)google\.""") to setOf("client", "channel"),
        Regex("""(^|\.)bing\.com$""") to setOf("pc", "form"),
        Regex("""(^|\.)duckduckgo\.com$""") to setOf("t"),
        Regex("""(^|\.)ebay\.""") to setOf("campid", "customid", "toolid", "mkevt", "mkcid", "mkrid"),
        Regex("""(^|\.)amazon\.""") to setOf("tag", "sourceid"),
        Regex("""(^|\.)yandex\.""") to setOf("clid"),
        Regex("""(^|\.)ecosia\.org$""") to setOf("tt"),
        Regex("""(^|\.)yahoo\.co\.jp$""") to setOf("fr"),
        Regex("""(^|\.)mercadolibre\.""") to setOf("source"),
        Regex("""(^|\.)coccoc\.com$""") to setOf("utm_source", "s"),
        Regex("""(^|\.)salidzini\.lv$""") to setOf("utm_source"),
        Regex("""(^|\.)(wikipedia|mapy|seznam)\.(org|cz|com)$""") to setOf("sourceid"),
        Regex("""(^|\.)qwant\.com$""") to setOf("client"),
        Regex("""(^|\.)odpiralnicasi\.com$""") to setOf("source"),
        Regex("""(^|\.)gulesider\.no$""") to setOf("cmpid"),
        Regex("""(^|\.)ceneje\.si$""") to setOf("FF-SearchBox"),
        Regex("""(^|\.)daum\.net$""") to setOf("nil_ch"),
    )

    /**
     * Returns [url] without the partner parameters of the search engine it belongs to. The rest of
     * the URL, including the search terms placeholder, is left as it was.
     */
    fun strip(url: String): String {
        val schemeEnd = url.indexOf("://")
        val queryStart = url.indexOf('?')
        if (schemeEnd < 0 || queryStart < schemeEnd + 3) return url

        val host = url.substring(schemeEnd + 3, queryStart)
            .takeWhile { it != '/' && it != '#' }
            .substringBefore(':')
            .lowercase()
        val names = rules.firstOrNull { it.first.containsMatchIn(host) }?.second ?: return url

        val fragmentStart = url.indexOf('#', queryStart).let { if (it < 0) url.length else it }
        val kept = url.substring(queryStart + 1, fragmentStart)
            .split('&')
            .filter { it.isNotEmpty() && it.substringBefore('=') !in names }
        val query = if (kept.isEmpty()) "" else "?" + kept.joinToString("&")
        return url.substring(0, queryStart) + query + url.substring(fragmentStart)
    }
}
