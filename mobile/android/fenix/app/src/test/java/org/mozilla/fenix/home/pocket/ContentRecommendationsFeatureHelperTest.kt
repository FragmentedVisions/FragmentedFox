/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.pocket

import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.components.support.locale.LocaleManager
import mozilla.components.support.test.robolectric.testContext
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ContentRecommendationsFeatureHelperTest {

    private val locales = listOf(
        Locale.Builder().setLanguage("ro").setRegion("RO").build(),
        Locale.Builder().setLanguage("en").setRegion("US").build(),
        Locale.Builder().setLanguage("en").setRegion("CA").build(),
        Locale.Builder().setLanguage("fr").build(),
        Locale.Builder().setLanguage("de").setRegion("AT").build(),
    )

    @Before
    @After
    fun cleanUp() {
        LocaleManager.resetToSystemDefault(context = testContext, localeUseCase = null)
    }

    @Test
    fun `GIVEN any locale WHEN pocket sponsored stories feature enabled check is invoked THEN return false`() {
        locales.forEach { locale ->
            LocaleManager.setNewLocale(testContext, localeUseCase = null, locale = locale)

            assertFalse(ContentRecommendationsFeatureHelper.isPocketSponsoredStoriesFeatureEnabled(testContext))
        }
    }

    @Test
    fun `GIVEN any locale WHEN content recommendations feature enabled check is invoked THEN return false`() {
        locales.forEach { locale ->
            LocaleManager.setNewLocale(testContext, localeUseCase = null, locale = locale)

            assertFalse(ContentRecommendationsFeatureHelper.isContentRecommendationsFeatureEnabled(testContext))
        }
    }
}
