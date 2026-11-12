/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ext

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.fenix.components.appstate.AppState
import org.mozilla.fenix.home.recentsyncedtabs.RecentSyncedTabState
import org.mozilla.fenix.utils.Settings

class AppStateTest {

    @Test
    fun `GIVEN recent tabs disabled in settings WHEN checking to show tabs THEN section should not be shown`() {
        val settings = mockk<Settings> {
            every { showRecentTabsFeature } returns false
        }

        val state = AppState()

        Assert.assertFalse(state.shouldShowRecentTabs(settings))
    }

    @Test
    fun `GIVEN only local tabs WHEN checking to show tabs THEN section should be shown`() {
        val settings = mockk<Settings> {
            every { showRecentTabsFeature } returns true
        }

        val state = AppState(recentTabs = listOf(mockk()))

        assertTrue(state.shouldShowRecentTabs(settings))
    }

    @Test
    fun `GIVEN only remote tabs WHEN checking to show tabs THEN section should be shown`() {
        val settings = mockk<Settings> {
            every { showRecentTabsFeature } returns true
        }

        val state = AppState(recentSyncedTabState = RecentSyncedTabState.Success(emptyList()))

        assertTrue(state.shouldShowRecentTabs(settings))
    }

    @Test
    fun `GIVEN local and remote tabs WHEN checking to show tabs THEN section should be shown`() {
        val settings = mockk<Settings> {
            every { showRecentTabsFeature } returns true
        }

        val state = AppState(
            recentTabs = listOf(mockk()),
            recentSyncedTabState = RecentSyncedTabState.Success(emptyList()),
        )

        assertTrue(state.shouldShowRecentTabs(settings))
    }
}
