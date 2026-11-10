/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.datachoices

import mozilla.components.lib.crash.store.CrashReportOption
import mozilla.components.lib.state.State

/**
 * Represents the state of the data collections screen
 *
 * @property selectedCrashOption The user's selected preference for handling crash reports.
 * @property itemToScrollTo Optional title of the settings item to scroll to upon opening
 * the data choices settings screen.
 */
internal data class DataChoicesState(
    val selectedCrashOption: CrashReportOption = CrashReportOption.Ask,
    val itemToScrollTo: String? = null,
) : State
