/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.datachoices

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.concept.engine.Engine
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.lib.crash.store.CrashReportOption
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.Store
import mozilla.components.service.nimbus.NimbusApi
import org.mozilla.fenix.components.metrics.MetricController
import org.mozilla.fenix.components.metrics.MetricServiceType
import org.mozilla.fenix.crashes.SettingsCrashReportCache
import org.mozilla.fenix.settings.SupportUtils
import org.mozilla.fenix.utils.Settings

@Suppress("LongParameterList")
internal class DataChoicesMiddleware(
    private val settings: Settings,
    private val nimbusSdk: NimbusApi,
    private val engine: Engine,
    private val metrics: MetricController,
    private val crashReporter: CrashReporter,
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
                        telemetryEnabled = settings.isTelemetryEnabled,
                        usagePingEnabled = settings.isDailyUsagePingEnabled,
                        crashReportOption = crashReportCache.getReportOption(),
                    ),
                )
            }
            is ChoiceAction.TelemetryClicked -> {
                updateTelemetryChoice()
            }
            is ChoiceAction.UsagePingClicked -> {
                updateUsageChoice()
            }
            is ChoiceAction.ReportOptionClicked -> scope.launch {
                updateCrashChoice(action.reportOption)
            }

            LearnMore.CrashLearnMoreClicked -> {
                learnMoreClicked(SupportUtils.SumoTopic.CRASH_REPORTS)
            }
            LearnMore.TelemetryLearnMoreClicked -> {
                learnMoreClicked(SupportUtils.SumoTopic.TECHNICAL_AND_INTERACTION_DATA)
            }
            LearnMore.UsagePingLearnMoreClicked -> {
                learnMoreClicked(SupportUtils.SumoTopic.USAGE_PING_SETTINGS)
            }
            is SettingsLoaded -> {}
            is ChoiceAction.ScrolledToItem -> {}
        }
    }

    private fun updateTelemetryChoice() {
        val newValue = !settings.isTelemetryEnabled
        settings.isTelemetryEnabled = newValue
        if (newValue) {
            metrics.start(MetricServiceType.Data)
            crashReporter.setTelemetryEnabled(true)
            if (!settings.hasUserDisabledExperimentation) {
                settings.isExperimentationEnabled = true
                nimbusSdk.experimentParticipation = true
            }
            engine.notifyTelemetryPrefChanged(true)
        } else {
            metrics.stop(MetricServiceType.Data)
            crashReporter.setTelemetryEnabled(false)
            settings.isExperimentationEnabled = false
            nimbusSdk.experimentParticipation = false
            engine.notifyTelemetryPrefChanged(false)
        }
        // Reset experiment identifiers on both opt-in and opt-out; it's likely
        // that in future we will need to pass in the new telemetry client_id
        // to this method when the user opts back in.
        nimbusSdk.resetTelemetryIdentifiers()
    }

    private fun updateUsageChoice() {
        val newValue = !settings.isDailyUsagePingEnabled
        settings.isDailyUsagePingEnabled = newValue
        with(metrics) {
            if (newValue) {
                start(MetricServiceType.UsageReporting)
            } else {
                stop(MetricServiceType.UsageReporting)
            }
        }
    }

    private suspend fun updateCrashChoice(newValue: CrashReportOption) {
        crashReportCache.setReportOption(newValue)
    }
}
