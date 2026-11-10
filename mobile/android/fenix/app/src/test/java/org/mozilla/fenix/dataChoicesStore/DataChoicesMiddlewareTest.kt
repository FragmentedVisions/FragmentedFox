package org.mozilla.fenix.dataChoicesStore

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import mozilla.components.lib.crash.store.CrashReportOption
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.crashes.SettingsCrashReportCache
import org.mozilla.fenix.settings.SupportUtils
import org.mozilla.fenix.settings.datachoices.ChoiceAction
import org.mozilla.fenix.settings.datachoices.DataChoicesMiddleware
import org.mozilla.fenix.settings.datachoices.DataChoicesState
import org.mozilla.fenix.settings.datachoices.DataChoicesStore
import org.mozilla.fenix.settings.datachoices.LearnMore
import org.mozilla.fenix.settings.datachoices.ViewCreated
import org.mozilla.fenix.utils.Settings

@RunWith(AndroidJUnit4::class)
class DataChoicesMiddlewareTest {

    private lateinit var settings: Settings
    private lateinit var crashReportCache: SettingsCrashReportCache

    @Before
    fun setup() {
        settings = mockk(relaxed = true)
        crashReportCache = mockk(relaxed = true)
    }

    @Test
    fun `when the view is created then the current state is loaded from cache`() = runTest {
        coEvery { crashReportCache.getReportOption() } returns CrashReportOption.Auto

        val store = makeStore(this)

        store.dispatch(ViewCreated)

        testScheduler.advanceUntilIdle()

        assertEquals(CrashReportOption.Auto, store.state.selectedCrashOption)
    }

    @Test
    fun `when a crash report option is selected then it is saved to the crash report cache`() =
        runTest {
            val store = makeStore(this)

            store.dispatch(ChoiceAction.ReportOptionClicked(CrashReportOption.Never))
            testScheduler.advanceUntilIdle()

            coVerify { crashReportCache.setReportOption(CrashReportOption.Never) }
            assertEquals(CrashReportOption.Never, store.state.selectedCrashOption)
        }

    @Test
    fun `when learn more is clicked then the corresponding help topic callback is invoked`() =
        runTest {
            var invokedTopic: SupportUtils.SumoTopic? = null
            val store = makeStore(scope = this) { topic -> invokedTopic = topic }

            store.dispatch(LearnMore.CrashLearnMoreClicked)
            assertEquals(SupportUtils.SumoTopic.CRASH_REPORTS, invokedTopic)
        }

    private fun makeStore(
        scope: CoroutineScope,
        learnMoreClicked: (SupportUtils.SumoTopic) -> Unit = {},
    ) = DataChoicesStore(
        initialState = DataChoicesState(),
        middleware = listOf(
            DataChoicesMiddleware(
                settings = settings,
                learnMoreClicked = learnMoreClicked,
                crashReportCache = crashReportCache,
                scope = scope,
            ),
        ),
    )
}
