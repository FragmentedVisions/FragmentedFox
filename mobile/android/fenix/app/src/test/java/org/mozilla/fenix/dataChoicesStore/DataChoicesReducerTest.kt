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
        telemetryEnabled = false,
        usagePingEnabled = false,
        selectedCrashOption = CrashReportOption.Ask,
    )

    @Test
    fun `when telemetry is clicked then the state of telemetry is toggled`() {
        val before = defaultState.copy(telemetryEnabled = false)
        val after = dataChoicesReducer(before, ChoiceAction.TelemetryClicked)
        assertEquals(true, after.telemetryEnabled)
    }

    @Test
    fun `when usage ping is clicked then usagePingEnabled is toggled`() {
        val before = defaultState.copy(usagePingEnabled = false)
        val after = dataChoicesReducer(before, ChoiceAction.UsagePingClicked)
        assertEquals(true, after.usagePingEnabled)
    }

    @Test
    fun `when a crash report option is selected then the selectedCrashOption is updated`() {
        val before = defaultState.copy(selectedCrashOption = CrashReportOption.Never)
        val after = dataChoicesReducer(before, ChoiceAction.ReportOptionClicked(CrashReportOption.Auto))
        assertEquals(CrashReportOption.Auto, after.selectedCrashOption)
    }

    @Test
    fun `when settings are loaded then the entire settings state is replaced`() {
        val action = SettingsLoaded(
            telemetryEnabled = true,
            usagePingEnabled = true,
            crashReportOption = CrashReportOption.Never,
        )
        val after = dataChoicesReducer(defaultState, action)
        assertEquals(true, after.telemetryEnabled)
        assertEquals(true, after.usagePingEnabled)
        assertEquals(CrashReportOption.Never, after.selectedCrashOption)
    }
}
