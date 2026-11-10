/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components

import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.engine.mediasession.MediaSession
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.Store

private const val BACKGROUND_TABS_KEPT_ACTIVE = 1

/**
 * Whenever the selected tab changes, suspends every tab except the selected one and the most
 * recently used other one. A suspended tab runs nothing and is rebuilt from its saved state when it
 * is opened again. Tabs with typed form input and tabs that are still loading are left alone, and so
 * are tabs playing media when [keepMediaTabsActive] is true.
 *
 * @property isEnabled Whether the limit is turned on.
 * @property keepMediaTabsActive Whether tabs that are playing media stay active.
 */
class BackgroundTabsLimitMiddleware(
    private val isEnabled: () -> Boolean,
    private val keepMediaTabsActive: () -> Boolean,
) : Middleware<BrowserState, BrowserAction> {

    override fun invoke(
        store: Store<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction,
    ) {
        val previousSelectedTabId = store.state.selectedTabId
        next(action)

        val selectedTabId = store.state.selectedTabId
        if (selectedTabId == null || selectedTabId == previousSelectedTabId || !isEnabled()) {
            return
        }

        tabsToSuspend(store.state).forEach { tab ->
            store.dispatch(EngineAction.SuspendEngineSessionAction(tab.id))
        }
    }

    private fun tabsToSuspend(state: BrowserState): List<TabSessionState> =
        state.tabs
            .filter { it.id != state.selectedTabId && it.engineState.engineSession != null }
            .sortedByDescending { it.lastAccess }
            .drop(BACKGROUND_TABS_KEPT_ACTIVE)
            .filterNot { tab ->
                val playingMedia = tab.mediaSessionState?.playbackState == MediaSession.PlaybackState.PLAYING
                tab.content.hasFormData || tab.content.loading || (keepMediaTabsActive() && playingMedia)
            }
}
