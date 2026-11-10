package org.mozilla.fenix.dataChoicesStore

import mozilla.components.lib.crash.store.CrashReportOption
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mozilla.fenix.settings.datachoices.ChoiceAction
import org.mozilla.fenix.settings.datachoices.DataChoicesState
import org.mozilla.fenix.settings.datachoices.SettingsLoaded
import org.mozilla.fenix.settings.datachoices.dataChoicesReducer

class DataChoicesReducerTest {

    private val defaultState = DataChoicesState(
        selectedCrashOption = CrashReportOption.Ask,
    )

    @Test
    fun `when a crash report option is selected then the selectedCrashOption is updated`() {
        val before = defaultState.copy(selectedCrashOption = CrashReportOption.Never)
        val after = dataChoicesReducer(before, ChoiceAction.ReportOptionClicked(CrashReportOption.Auto))
        assertEquals(CrashReportOption.Auto, after.selectedCrashOption)
    }

    @Test
    fun `when settings are loaded then the entire settings state is replaced`() {
        val action = SettingsLoaded(
            crashReportOption = CrashReportOption.Never,
        )
        val after = dataChoicesReducer(defaultState, action)
        assertEquals(CrashReportOption.Never, after.selectedCrashOption)
    }
}
