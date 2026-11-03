/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.addons

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import mozilla.components.concept.engine.webextension.InstallationMethod
import mozilla.components.feature.addons.AddonManager
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.fenix.R
import org.mozilla.fenix.utils.Settings

/**
 * Offers once to install the recommended content blockers from the add-ons site.
 * Nothing is installed unless the user accepts the dialog.
 */
class DefaultExtensionsPrompt(
    private val context: Context,
    private val settings: Settings,
    private val addonManager: AddonManager,
) {
    private val logger = Logger("DefaultExtensionsPrompt")

    fun showIfNeeded() {
        if (settings.hasSeenDefaultExtensionsPrompt) {
            return
        }
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.default_extensions_title)
            .setMessage(R.string.default_extensions_message)
            .setPositiveButton(R.string.default_extensions_install) { _, _ -> install() }
            .setNegativeButton(R.string.default_extensions_not_now, null)
            .show()
        settings.hasSeenDefaultExtensionsPrompt = true
    }

    private fun install() {
        EXTENSION_URLS.forEach { url ->
            addonManager.installAddon(
                url = url,
                installationMethod = InstallationMethod.MANAGER,
                onSuccess = { addon ->
                    addonManager.setAddonAllowedInPrivateBrowsing(
                        addon = addon,
                        allowed = true,
                        onError = { logger.error("Failed to allow ${addon.id} in private tabs", it) },
                    )
                },
                onError = { logger.error("Failed to install $url", it) },
            )
        }
    }

    private companion object {
        const val AMO_DOWNLOADS = "https://addons.mozilla.org/firefox/downloads/latest"
        val EXTENSION_URLS = listOf(
            "$AMO_DOWNLOADS/ublock-origin/latest.xpi",
            "$AMO_DOWNLOADS/privacy-badger17/latest.xpi",
        )
    }
}
