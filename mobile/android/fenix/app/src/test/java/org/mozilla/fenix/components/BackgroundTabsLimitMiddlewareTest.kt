/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components

import io.mockk.mockk
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.EngineState
import mozilla.components.browser.state.state.MediaSessionState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.mediasession.MediaSession
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.Store
import org.junit.Assert.assertEquals
import org.junit.Test

class BackgroundTabsLimitMiddlewareTest {

    private class SuspendRecorder : Middleware<BrowserState, BrowserAction> {
        val suspended = mutableListOf<String>()

        override fun invoke(
            store: Store<BrowserState, BrowserAction>,
            next: (BrowserAction) -> Unit,
            action: BrowserAction,
        ) {
            if (action is EngineAction.SuspendEngineSessionAction) {
                suspended.add(action.tabId)
            }
            next(action)
        }
    }

    private fun activeTab(id: String, lastAccess: Long): TabSessionState =
        createTab("https://example.org/$id", id = id).copy(
            lastAccess = lastAccess,
            engineState = EngineState(engineSession = mockk<EngineSession>(relaxed = true)),
        )

    private fun storeWith(
        tabs: List<TabSessionState>,
        recorder: SuspendRecorder,
        enabled: Boolean = true,
        keepMedia: Boolean = true,
    ) = BrowserStore(
        initialState = BrowserState(tabs = tabs, selectedTabId = tabs.first().id),
        middleware = listOf(
            BackgroundTabsLimitMiddleware(isEnabled = { enabled }, keepMediaTabsActive = { keepMedia }),
            recorder,
        ),
    )

    @Test
    fun `WHEN another tab is selected THEN only the selected and the most recent other tab stay active`() {
        val recorder = SuspendRecorder()
        val store = storeWith(
            listOf(activeTab("a", 10), activeTab("b", 40), activeTab("c", 30), activeTab("d", 20)),
            recorder,
        )

        store.dispatch(TabListAction.SelectTabAction("b"))

        assertEquals(setOf("a", "d"), recorder.suspended.toSet())
    }

    @Test
    fun `WHEN the limit is turned off THEN no tab is suspended`() {
        val recorder = SuspendRecorder()
        val store = storeWith(
            listOf(activeTab("a", 10), activeTab("b", 40), activeTab("c", 30), activeTab("d", 20)),
            recorder,
            enabled = false,
        )

        store.dispatch(TabListAction.SelectTabAction("b"))

        assertEquals(emptyList<String>(), recorder.suspended)
    }

    @Test
    fun `WHEN the selected tab is selected again THEN no tab is suspended`() {
        val recorder = SuspendRecorder()
        val store = storeWith(
            listOf(activeTab("a", 10), activeTab("b", 40), activeTab("c", 30)),
            recorder,
        )

        store.dispatch(TabListAction.SelectTabAction("a"))

        assertEquals(emptyList<String>(), recorder.suspended)
    }

    @Test
    fun `WHEN a tab has no engine session THEN it is not suspended`() {
        val recorder = SuspendRecorder()
        val store = storeWith(
            listOf(
                activeTab("a", 10),
                activeTab("b", 40),
                activeTab("c", 30),
                createTab("https://example.org/d", id = "d", lastAccess = 20),
            ),
            recorder,
        )

        store.dispatch(TabListAction.SelectTabAction("b"))

        assertEquals(listOf("a"), recorder.suspended)
    }

    @Test
    fun `WHEN tabs have form data or are loading THEN they are not suspended`() {
        val recorder = SuspendRecorder()
        val withForm = activeTab("c", 30).let { it.copy(content = it.content.copy(hasFormData = true)) }
        val loading = activeTab("d", 20).let { it.copy(content = it.content.copy(loading = true)) }
        val store = storeWith(
            listOf(
                activeTab("a", 10),
                activeTab("b", 40),
                activeTab("f", 35),
                withForm,
                loading,
                activeTab("e", 5),
            ),
            recorder,
        )

        store.dispatch(TabListAction.SelectTabAction("b"))

        assertEquals(setOf("a", "e"), recorder.suspended.toSet())
    }

    @Test
    fun `WHEN a tab is playing media THEN it is only kept if media tabs stay active`() {
        val playing = activeTab("c", 30).copy(
            mediaSessionState = MediaSessionState(
                controller = mockk<MediaSession.Controller>(relaxed = true),
                playbackState = MediaSession.PlaybackState.PLAYING,
            ),
        )
        val tabs = listOf(activeTab("a", 10), activeTab("b", 40), playing, activeTab("f", 35))

        val keepRecorder = SuspendRecorder()
        storeWith(tabs, keepRecorder, keepMedia = true).dispatch(TabListAction.SelectTabAction("b"))
        assertEquals(listOf("a"), keepRecorder.suspended)

        val dropRecorder = SuspendRecorder()
        storeWith(tabs, dropRecorder, keepMedia = false).dispatch(TabListAction.SelectTabAction("b"))
        assertEquals(setOf("a", "c"), dropRecorder.suspended.toSet())
    }
}
