/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.onboarding.view

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mozilla.fenix.R

class OnboardingMapperTest {

    @Test
    fun `GIVEN a default browser page WHEN mapToOnboardingPageState is called THEN creates the expected OnboardingPageState`() {
        val expected = OnboardingPageState(
            imageRes = R.drawable.ic_onboarding_welcome,
            title = "default browser title",
            description = "default browser body with link text",
            primaryButton = Action("default browser primary button text", unitLambda),
            secondaryButton = Action("default browser secondary button text", unitLambda),
        )

        val onboardingPageUiData = OnboardingPageUiData(
            type = OnboardingPageUiData.Type.DEFAULT_BROWSER,
            imageRes = R.drawable.ic_onboarding_welcome,
            title = "default browser title",
            description = "default browser body with link text",
            primaryButtonLabel = "default browser primary button text",
            secondaryButtonLabel = "default browser secondary button text",
        )
        val actual = mapToOnboardingPageState(
            onboardingPageUiData = onboardingPageUiData,
            onMakeFirefoxDefaultClick = unitLambda,
            onMakeFirefoxDefaultSkipClick = unitLambda,
            onTermsOfServiceButtonClick = {},
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `GIVEN a terms of service page WHEN mapToOnboardingPageState is called THEN creates the expected OnboardingPageState`() {
        val termsOfService = OnboardingTermsOfService(
            lineOneText = "line one",
            lineOneLinkText = "line one link",
            lineOneLinkUrl = "https://example.org/one",
            lineTwoText = "line two",
            lineTwoLinkText = "line two link",
            lineTwoLinkUrl = "https://example.org/two",
            lineThreeText = "line three",
        )
        val expected = OnboardingPageState(
            imageRes = R.drawable.ic_onboarding_welcome,
            title = "terms of service title",
            description = "",
            primaryButton = Action("terms of service primary button text", unitLambda),
            termsOfService = termsOfService,
        )

        val onboardingPageUiData = OnboardingPageUiData(
            type = OnboardingPageUiData.Type.TERMS_OF_SERVICE,
            imageRes = R.drawable.ic_onboarding_welcome,
            title = "terms of service title",
            description = "",
            primaryButtonLabel = "terms of service primary button text",
            termsOfService = termsOfService,
        )
        val actual = mapToOnboardingPageState(
            onboardingPageUiData = onboardingPageUiData,
            onMakeFirefoxDefaultClick = unitLambda,
            onMakeFirefoxDefaultSkipClick = unitLambda,
            onTermsOfServiceButtonClick = unitLambda,
        )

        assertEquals(expected, actual)
    }
}

private val unitLambda = { dummyUnitFunc() }

private fun dummyUnitFunc() {}
