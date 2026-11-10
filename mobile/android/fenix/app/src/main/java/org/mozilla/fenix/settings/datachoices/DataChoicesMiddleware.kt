/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.datachoices

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.lib.crash.store.CrashReportOption
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.Store
import org.mozilla.fenix.crashes.SettingsCrashReportCache
import org.mozilla.fenix.settings.SupportUtils
import org.mozilla.fenix.utils.Settings

internal class DataChoicesMiddleware(
    private val settings: Settings,
    private val learnMoreClicked: (sumoTopic: SupportUtils.SumoTopic) -> Unit,
    private val crashReportCache: SettingsCrashReportCache = SettingsCrashReportCache(settings),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : Middleware<DataChoicesState, DataChoicesAction> {

    override fun invoke(
        store: Store<DataChoicesState, DataChoicesAction>,
        next: (DataChoicesAction) -> Unit,
        action: DataChoicesAction,
    ) {
        next(action)

        when (action) {
            is ViewCreated -> scope.launch {
                store.dispatch(
                    SettingsLoaded(
                        crashReportOption = crashReportCache.getReportOption(),
                    ),
                )
            }
            is ChoiceAction.ReportOptionClicked -> scope.launch {
                updateCrashChoice(action.reportOption)
            }

            LearnMore.CrashLearnMoreClicked -> {
                learnMoreClicked(SupportUtils.SumoTopic.CRASH_REPORTS)
            }
            is SettingsLoaded -> {}
            is ChoiceAction.ScrolledToItem -> {}
        }
    }

    private suspend fun updateCrashChoice(newValue: CrashReportOption) {
        crashReportCache.setReportOption(newValue)
    }
}
