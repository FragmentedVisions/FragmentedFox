/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.datachoices

internal fun dataChoicesReducer(state: DataChoicesState, action: DataChoicesAction) = when (action) {
    is ChoiceAction.ReportOptionClicked -> {
        state.copy(selectedCrashOption = action.reportOption)
    }
    is ChoiceAction.ScrolledToItem -> {
        state.copy(itemToScrollTo = null)
    }
    is SettingsLoaded ->
        state.copy(
            selectedCrashOption = action.crashReportOption,
        )
    LearnMore.CrashLearnMoreClicked,
    is ViewCreated,
    -> { state }
}
