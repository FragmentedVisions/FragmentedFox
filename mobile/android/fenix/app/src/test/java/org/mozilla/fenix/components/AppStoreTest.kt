/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.components

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import mozilla.components.concept.sync.DeviceType
import mozilla.components.feature.tab.collections.TabCollection
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.service.fxa.manager.FxaAccountManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.R
import org.mozilla.fenix.browser.browsingmode.BrowsingMode
import org.mozilla.fenix.browser.browsingmode.BrowsingModeManager
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.components.appstate.AppState
import org.mozilla.fenix.components.appstate.filterOut
import org.mozilla.fenix.components.appstate.setup.checklist.ChecklistItem
import org.mozilla.fenix.components.appstate.setup.checklist.SetupChecklistState
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.home.bookmarks.Bookmark
import org.mozilla.fenix.home.recentsyncedtabs.RecentSyncedTab
import org.mozilla.fenix.home.recentsyncedtabs.RecentSyncedTabState
import org.mozilla.fenix.home.recenttabs.RecentTab
import org.mozilla.fenix.home.recentvisits.RecentlyVisitedItem
import org.mozilla.fenix.home.recentvisits.RecentlyVisitedItem.RecentHistoryGroup
import org.mozilla.fenix.home.recentvisits.RecentlyVisitedItem.RecentHistoryHighlight
import org.mozilla.fenix.onboarding.FenixOnboarding
import mozilla.components.ui.icons.R as iconsR

@RunWith(AndroidJUnit4::class)
class AppStoreTest {
    private lateinit var context: Context
    private lateinit var accountManager: FxaAccountManager
    private lateinit var onboarding: FenixOnboarding
    private lateinit var browsingModeManager: BrowsingModeManager
    private lateinit var appState: AppState
    private lateinit var appStore: AppStore
    private lateinit var recentSyncedTabsList: List<RecentSyncedTab>

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        accountManager = mockk(relaxed = true)
        onboarding = mockk(relaxed = true)
        browsingModeManager = mockk(relaxed = true)
        recentSyncedTabsList = listOf(
            RecentSyncedTab(
                deviceDisplayName = "",
                deviceType = mockk(relaxed = true),
                title = "",
                url = "",
                previewImageUrl = null,
            ),
        )

        every { context.components.backgroundServices.accountManager } returns accountManager
        every { onboarding.userHasBeenOnboarded() } returns true
        every { browsingModeManager.mode } returns BrowsingMode.Normal

        appState = AppState(
            collections = emptyList(),
            expandedCollections = emptySet(),
            mode = browsingModeManager.mode,
            topSites = emptyList(),
            recentTabs = emptyList(),
            recentSyncedTabState = RecentSyncedTabState.Success(recentSyncedTabsList),
        )

        appStore = AppStore(appState)
    }

    @Test
    fun `Test toggling the mode in AppStore`() = runTest {
        // Verify that the default mode and tab states of the HomeFragment are correct.
        assertEquals(BrowsingMode.Normal, appStore.state.mode)

        // Change the AppStore to Private mode.
        appStore.dispatch(AppAction.BrowsingModeManagerModeChanged(BrowsingMode.Private))
        assertEquals(BrowsingMode.Private, appStore.state.mode)

        // Change the AppStore back to Normal mode.
        appStore.dispatch(AppAction.BrowsingModeManagerModeChanged(BrowsingMode.Normal))
        assertEquals(BrowsingMode.Normal, appStore.state.mode)
    }

    @Test
    fun `Test changing the collections in AppStore`() = runTest {
        assertEquals(0, appStore.state.collections.size)

        // Add 2 TabCollections to the AppStore.
        val tabCollections: List<TabCollection> = listOf(mockk(), mockk())
        appStore.dispatch(AppAction.CollectionsChange(tabCollections))

        assertEquals(tabCollections, appStore.state.collections)
    }

    @Test
    fun `Test changing the top sites in AppStore`() = runTest {
        assertEquals(0, appStore.state.topSites.size)

        // Add 2 TopSites to the AppStore.
        val topSites: List<TopSite> = listOf(mockk(), mockk())
        appStore.dispatch(AppAction.TopSitesChange(topSites))

        assertEquals(topSites, appStore.state.topSites)
    }

    @Test
    fun `Test changing the recent tabs in AppStore`() = runTest {
        val group1 = RecentHistoryGroup(title = "title1")
        val group2 = RecentHistoryGroup(title = "title2")
        val group3 = RecentHistoryGroup(title = "title3")
        val highlight = RecentHistoryHighlight(title = group2.title, "")
        appStore = AppStore(
            AppState(
                recentHistory = listOf(group1, group2, group3, highlight),
            ),
        )
        assertEquals(0, appStore.state.recentTabs.size)

        // Add 2 RecentTabs to the AppStore
        val recentTab1: RecentTab.Tab = mockk()
        val recentTabs: List<RecentTab> = listOf(recentTab1)
        appStore.dispatch(AppAction.RecentTabsChange(recentTabs))

        assertEquals(recentTabs, appStore.state.recentTabs)
        assertEquals(listOf(group1, group2, group3, highlight), appStore.state.recentHistory)
    }

    @Test
    fun `GIVEN initial state WHEN recent synced tab state is changed THEN state updated`() = runTest {
        appStore = AppStore(
            AppState(
                recentSyncedTabState = RecentSyncedTabState.None,
            ),
        )

        val loading = RecentSyncedTabState.Loading
        appStore.dispatch(AppAction.RecentSyncedTabStateChange(loading))
        assertEquals(loading, appStore.state.recentSyncedTabState)

        val recentSyncedTabs = listOf(RecentSyncedTab("device name", DeviceType.DESKTOP, "title", "url", null))
        val success = RecentSyncedTabState.Success(recentSyncedTabs)
        appStore.dispatch(AppAction.RecentSyncedTabStateChange(success))
        assertEquals(success, appStore.state.recentSyncedTabState)
        assertEquals(recentSyncedTabs, (appStore.state.recentSyncedTabState as RecentSyncedTabState.Success).tabs)
    }

    @Test
    fun `Test changing the history metadata in AppStore`() = runTest {
        assertEquals(0, appStore.state.recentHistory.size)

        val historyMetadata: List<RecentHistoryGroup> = listOf(mockk(), mockk())
        appStore.dispatch(AppAction.RecentHistoryChange(historyMetadata))

        assertEquals(historyMetadata, appStore.state.recentHistory)
    }

    @Test
    fun `Test removing a history highlight from AppStore`() = runTest {
        val g1 = RecentHistoryGroup(title = "group One")
        val g2 = RecentHistoryGroup(title = "grup two")
        val h1 = RecentHistoryHighlight(title = "highlight One", url = "url1")
        val h2 = RecentHistoryHighlight(title = "highlight two", url = "url2")
        val recentHistoryState = AppState(
            recentHistory = listOf(g1, g2, h1, h2),
        )
        appStore = AppStore(recentHistoryState)

        appStore.dispatch(AppAction.RemoveRecentHistoryHighlight("invalid"))
        assertEquals(recentHistoryState, appStore.state)

        appStore.dispatch(AppAction.RemoveRecentHistoryHighlight(h1.title))
        assertEquals(recentHistoryState, appStore.state)

        appStore.dispatch(AppAction.RemoveRecentHistoryHighlight(h1.url))
        assertEquals(
            recentHistoryState.copy(recentHistory = listOf(g1, g2, h2)),
            appStore.state,
        )
    }

    @Test
    fun `Test disbanding search group in AppStore`() = runTest {
        val g1 = RecentHistoryGroup(title = "test One")
        val g2 = RecentHistoryGroup(title = "test two")
        val h1 = RecentHistoryHighlight(title = "highlight One", url = "url1")
        val h2 = RecentHistoryHighlight(title = "highlight two", url = "url2")
        val recentHistory: List<RecentlyVisitedItem> = listOf(g1, g2, h1, h2)
        appStore.dispatch(AppAction.RecentHistoryChange(recentHistory))
        assertEquals(recentHistory, appStore.state.recentHistory)

        appStore.dispatch(AppAction.DisbandSearchGroupAction("Test one"))
        assertEquals(listOf(g2, h1, h2), appStore.state.recentHistory)
    }

    @Test
    fun `Test changing the expanded collections in AppStore`() = runTest {
        val collection: TabCollection = mockk<TabCollection>().apply {
            every { id } returns 0
        }

        // Expand the given collection.
        appStore.dispatch(AppAction.CollectionsChange(listOf(collection)))
        appStore.dispatch(AppAction.CollectionExpanded(collection, true))

        assertTrue(appStore.state.expandedCollections.contains(collection.id))
        assertEquals(1, appStore.state.expandedCollections.size)
    }

    @Test
    fun `Test changing the collections, mode, recent tabs and bookmarks, history metadata, top sites and recent synced tabs in the AppStore`() =
        runTest {
            // Verify that the default state of the HomeFragment is correct.
            assertEquals(0, appStore.state.collections.size)
            assertEquals(0, appStore.state.topSites.size)
            assertEquals(0, appStore.state.recentTabs.size)
            assertEquals(0, appStore.state.bookmarks.size)
            assertEquals(0, appStore.state.recentHistory.size)
            assertEquals(BrowsingMode.Normal, appStore.state.mode)
            assertEquals(
                RecentSyncedTabState.Success(recentSyncedTabsList),
                appStore.state.recentSyncedTabState,
            )

            val collections: List<TabCollection> = listOf(mockk())
            val topSites: List<TopSite> = listOf(mockk(), mockk())
            val recentTabs: List<RecentTab> = listOf(mockk(), mockk())
            val bookmarks: List<Bookmark> = listOf(mockk(), mockk())
            val group1 = RecentHistoryGroup(title = "test One")
            val group2 = RecentHistoryGroup(title = "testSearchTerm")
            val group3 = RecentHistoryGroup(title = "test two")
            val highlight = RecentHistoryHighlight(group2.title, "")
            val recentHistory: List<RecentlyVisitedItem> = listOf(group1, group2, group3, highlight)
            val recentSyncedTab = RecentSyncedTab(
                deviceDisplayName = "device1",
                deviceType = mockk(relaxed = true),
                title = "1",
                url = "",
                previewImageUrl = null,
            )
            val recentSyncedTabState: RecentSyncedTabState =
                RecentSyncedTabState.Success(recentSyncedTabsList + recentSyncedTab)

            appStore.dispatch(
                AppAction.Change(
                    collections = collections,
                    mode = BrowsingMode.Private,
                    topSites = topSites,
                    recentTabs = recentTabs,
                    bookmarks = bookmarks,
                    recentHistory = recentHistory,
                    recentSyncedTabState = recentSyncedTabState,
                ),
            )

            assertEquals(collections, appStore.state.collections)
            assertEquals(topSites, appStore.state.topSites)
            assertEquals(recentTabs, appStore.state.recentTabs)
            assertEquals(bookmarks, appStore.state.bookmarks)
            assertEquals(listOf(group1, group2, group3, highlight), appStore.state.recentHistory)
            assertEquals(BrowsingMode.Private, appStore.state.mode)
            assertEquals(
                recentSyncedTabState,
                appStore.state.recentSyncedTabState,
            )
        }

    @Test
    fun `Test filtering out search groups`() {
        val group1 = RecentHistoryGroup("title1")
        val group2 = RecentHistoryGroup("title2")
        val group3 = RecentHistoryGroup("title3")
        val highLight1 = RecentHistoryHighlight("title1", "")
        val highLight2 = RecentHistoryHighlight("title2", "")
        val highLight3 = RecentHistoryHighlight("title3", "")
        val recentHistory = listOf(group1, highLight1, group2, highLight2, group3, highLight3)

        assertEquals(recentHistory, recentHistory.filterOut(null))
        assertEquals(recentHistory, recentHistory.filterOut(""))
        assertEquals(recentHistory, recentHistory.filterOut(" "))
        assertEquals(recentHistory - group2, recentHistory.filterOut("Title2"))
        assertEquals(recentHistory - group3, recentHistory.filterOut("title3"))
    }

    @Test
    fun `WHEN init action is dispatched THEN the setup checklist state remains the same`() {
        val appState = AppState(setupChecklistState = SetupChecklistState())

        appStore.dispatch(AppAction.SetupChecklistAction.Init)

        assertEquals(SetupChecklistState(), appState.setupChecklistState)
    }

    @Test
    fun `WHEN closed action is dispatched THEN the setup checklist state visible value is updated`() {
        val appState = AppState(setupChecklistState = SetupChecklistState())

        appStore.dispatch(AppAction.SetupChecklistAction.Closed)

        assertEquals(SetupChecklistState(isVisible = true), appState.setupChecklistState)
    }

    @Test
    fun `WHEN a group item is clicked action is dispatched THEN the group's expanded state is updated`() {
        val expandedGroup = ChecklistItem.Group(
            title = R.string.setup_checklist_group_essentials,
            tasks = listOf(
                ChecklistItem.Task(
                    type = ChecklistItem.Task.Type.SET_AS_DEFAULT,
                    title = R.string.setup_checklist_task_default_browser,
                    icon = iconsR.drawable.mozac_ic_extension_24,
                    isCompleted = false,
                ),
            ),
            isExpanded = true,
        )
        val collapsedGroup = ChecklistItem.Group(
            title = R.string.setup_checklist_group_essentials,
            tasks = listOf(
                ChecklistItem.Task(
                    type = ChecklistItem.Task.Type.INSTALL_SEARCH_WIDGET,
                    title = R.string.setup_checklist_task_search_widget_2,
                    icon = iconsR.drawable.mozac_ic_extension_24,
                    isCompleted = false,
                ),
            ),
            isExpanded = false,
        )

        val appState = AppState(
            setupChecklistState = SetupChecklistState(
                checklistItems = listOf(
                    expandedGroup,
                    collapsedGroup,
                ),
            ),
        )
        val store = AppStore(appState)

        // Verify that the expanded group is expanded, and the other one is not
        assertTrue((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Group).isExpanded)
        assertFalse((store.state.setupChecklistState!!.checklistItems[1] as ChecklistItem.Group).isExpanded)

        store.dispatch(AppAction.SetupChecklistAction.ChecklistItemClicked(collapsedGroup))

        // Verify that the expanded group was collapsed, and the other one got expanded
        assertFalse((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Group).isExpanded)
        assertTrue((store.state.setupChecklistState!!.checklistItems[1] as ChecklistItem.Group).isExpanded)
    }

    @Test
    fun `WHEN a task item is clicked action is dispatched THEN the task's completed state is not updated`() {
        val task = ChecklistItem.Task(
            type = ChecklistItem.Task.Type.EXPLORE_EXTENSION,
            title = R.string.setup_checklist_task_default_browser,
            icon = iconsR.drawable.mozac_ic_extension_24,
            isCompleted = false,
        )

        val appState =
            AppState(setupChecklistState = SetupChecklistState(checklistItems = listOf(task)))
        val store = AppStore(appState)
        store.dispatch(AppAction.SetupChecklistAction.ChecklistItemClicked(task))

        assertFalse((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Task).isCompleted)
    }

    @Test
    fun `WHEN a task preference updated action is dispatched THEN the task's completed state is updated`() {
        val task = ChecklistItem.Task(
            type = ChecklistItem.Task.Type.EXPLORE_EXTENSION,
            title = R.string.setup_checklist_task_default_browser,
            icon = iconsR.drawable.mozac_ic_extension_24,
            isCompleted = false,
        )

        val appState =
            AppState(setupChecklistState = SetupChecklistState(checklistItems = listOf(task)))
        val store = AppStore(appState)
        store.dispatch(AppAction.SetupChecklistAction.TaskPreferenceUpdated(task.type, true))

        assertTrue((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Task).isCompleted)

        store.dispatch(AppAction.SetupChecklistAction.TaskPreferenceUpdated(task.type, false))

        assertFalse((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Task).isCompleted)
    }

    @Test
    fun `WHEN a groups task preference is updated THEN only the updated task's completed state is updated`() {
        val updatedTask = ChecklistItem.Task(
            type = ChecklistItem.Task.Type.SET_AS_DEFAULT,
            title = R.string.setup_checklist_task_default_browser,
            icon = iconsR.drawable.mozac_ic_extension_24,
            isCompleted = false,
        )
        val nonUpdatedTask = ChecklistItem.Task(
            type = ChecklistItem.Task.Type.INSTALL_SEARCH_WIDGET,
            title = R.string.setup_checklist_task_search_widget_2,
            icon = iconsR.drawable.mozac_ic_extension_24,
            isCompleted = false,
        )
        val group = ChecklistItem.Group(
            title = R.string.setup_checklist_group_essentials,
            tasks = listOf(updatedTask, nonUpdatedTask),
            isExpanded = true,
        )

        val appState =
            AppState(setupChecklistState = SetupChecklistState(checklistItems = listOf(group)))
        val store = AppStore(appState)
        store.dispatch(AppAction.SetupChecklistAction.TaskPreferenceUpdated(updatedTask.type, true))

        assertTrue((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Group).tasks[0].isCompleted)
        assertFalse((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Group).tasks[1].isCompleted)

        store.dispatch(
            AppAction.SetupChecklistAction.TaskPreferenceUpdated(
                updatedTask.type,
                false,
            ),
        )

        assertFalse((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Group).tasks[0].isCompleted)
        assertFalse((store.state.setupChecklistState!!.checklistItems[0] as ChecklistItem.Group).tasks[1].isCompleted)
    }
}
