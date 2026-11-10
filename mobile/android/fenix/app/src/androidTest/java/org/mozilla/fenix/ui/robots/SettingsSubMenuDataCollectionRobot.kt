/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui.robots

import android.util.Log
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.mozilla.fenix.R
import org.mozilla.fenix.helpers.Constants.TAG
import org.mozilla.fenix.helpers.DataGenerationHelper.getStringResource
import org.mozilla.fenix.helpers.MatcherHelper.assertItemIsChecked
import org.mozilla.fenix.helpers.MatcherHelper.assertUIObjectExists
import org.mozilla.fenix.helpers.MatcherHelper.itemContainingText
import org.mozilla.fenix.helpers.MatcherHelper.itemWithDescription
import org.mozilla.fenix.helpers.MatcherHelper.itemWithResId
import org.mozilla.fenix.helpers.click
import mozilla.components.lib.crash.R as crashR

/**
 * Implementation of Robot Pattern for the settings Data Collection sub menu.
 */
class SettingsSubMenuDataCollectionRobot {

    fun verifyDataCollectionView(
        composeTestRule: ComposeTestRule,
        isAskBeforeSendingCrashReportsEnabled: Boolean = true,
        isAutomaticallySendCrashReportsEnabled: Boolean = false,
        isNeverSendCrashReportsEnabled: Boolean = false,
    ) {
        Log.i(TAG, "verifyDataCollectionView: Waiting for compose test rule to be idle")
        composeTestRule.waitForIdle()
        Log.i(TAG, "verifyDataCollectionView: Waited for compose test rule to be idle")
        assertUIObjectExists(
            // Toolbar items
            goBackButton(),
            itemContainingText(getStringResource(R.string.preferences_data_collection)),
        )
        // Crash reports section
        verifyTheCrashReportsSection(composeTestRule)

        // Crash reports radio buttons
        verifyTheCrashReportOptionStates(
            composeTestRule,
            isAskBeforeSendingCrashReportsEnabled,
            isAutomaticallySendCrashReportsEnabled,
            isNeverSendCrashReportsEnabled,
        )
    }

    fun verifyTheCrashReportsSection(composeTestRule: ComposeTestRule) {
        Log.i(TAG, "verifyTheCrashReportsSection: Waiting for compose test rule to be idle")
        composeTestRule.waitForIdle()
        Log.i(TAG, "verifyTheCrashReportsSection: Waited for compose test rule to be idle")

        assertUIObjectExists(
            itemContainingText(getStringResource(R.string.crash_reports_data_category)),
        itemContainingText(getStringResource(R.string.crash_reporting_description)),
        itemContainingText(getStringResource(crashR.string.crash_reporting_ask)),
        itemContainingText(getStringResource(crashR.string.crash_reporting_auto)),
        itemContainingText(getStringResource(crashR.string.crash_reporting_never)),
        )
    }

    fun verifyTheCrashReportOptionStates(
        composeTestRule: ComposeTestRule,
        isAskBeforeSendingCrashReportsEnabled: Boolean = true,
        isAutomaticallySendCrashReportsEnabled: Boolean = false,
        isNeverSendCrashReportsEnabled: Boolean = false,
    ) {
        Log.i(TAG, "verifyTheCrashReportOptionStates: Waiting for compose test rule to be idle")
        composeTestRule.waitForIdle()
        Log.i(TAG, "verifyTheCrashReportOptionStates: Waited for compose test rule to be idle")

        // Crash reports item state
        assertItemIsChecked(itemWithResId("data.collection.Ask.option"), isChecked = isAskBeforeSendingCrashReportsEnabled)
        assertItemIsChecked(itemWithResId("data.collection.Auto.option"), isChecked = isAutomaticallySendCrashReportsEnabled)
        assertItemIsChecked(itemWithResId("data.collection.Never.option"), isChecked = isNeverSendCrashReportsEnabled)
    }

    fun clickTheCrashReportsRadioButton(composeTestRule: ComposeTestRule, crashReportsRadioButton: String) {
        Log.i(TAG, "clickTheCrashReportsRadioButton: Trying to click the $crashReportsRadioButton radio button")
        when (crashReportsRadioButton) {
            "Ask before sending" -> composeTestRule.onNodeWithTag("Ask before sending.radio.button", useUnmergedTree = true).performClick()
            "Send automatically" -> composeTestRule.onNodeWithTag("Send automatically.radio.button", useUnmergedTree = true).performClick()
            "Never send" -> composeTestRule.onNodeWithTag("Never send.radio.button", useUnmergedTree = true).performClick()
        }
        Log.i(TAG, "clickTheCrashReportsRadioButton: Clicked the $crashReportsRadioButton radio button")
    }

    class Transition {
        fun goBack(interact: SettingsRobot.() -> Unit): SettingsRobot.Transition {
            Log.i(TAG, "goBack: Trying to click the navigate up toolbar button")
            goBackButton().click()
            Log.i(TAG, "goBack: Clicked the navigate up toolbar button")

            SettingsRobot().interact()
            return SettingsRobot.Transition()
        }
    }
}

private fun goBackButton() = itemWithDescription("Navigate up")
