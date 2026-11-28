/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.onboarding

import mozilla.components.support.base.log.logger.Logger
import org.mozilla.fenix.GleanMetrics.Onboarding
import org.mozilla.fenix.GleanMetrics.Pings
import org.mozilla.fenix.GleanMetrics.TermsOfUse
import org.mozilla.fenix.onboarding.view.OnboardingPageUiData
import org.mozilla.fenix.termsofuse.TOU_VERSION
import org.mozilla.fenix.termsofuse.store.Surface

/**
 * Abstraction responsible for recording telemetry events for Onboarding.
 */
class OnboardingTelemetryRecorder(
    private val onboardingReason: OnboardingReason,
    private val installSource: String,
) {
    val logger = Logger("OnboardingTelemetryRecorder")

    /**
     * Records "onboarding_completed" telemetry event and sends the onboarding ping.
     * @param sequenceId The identifier of the onboarding sequence shown to the user.
     * @param sequencePosition The sequence position of the page on which the completed event occurred.
     * @param dismissedMethod The method used to dismiss the onboarding flow.
     */
    fun onOnboardingComplete(
        sequenceId: String,
        sequencePosition: String,
        dismissedMethod: DismissedMethod = DismissedMethod.COMPLETED,
        ) {
        logger.debug(
            "Recording onboarding completed event, sequenceId: $sequenceId, " +
                "sequencePosition: $sequencePosition, dismissedMethod: $dismissedMethod",
        )

        Onboarding.completed.record(
            Onboarding.CompletedExtra(
                sequenceId = sequenceId,
                sequencePosition = sequencePosition,
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
        Onboarding.dismissed.record(
            Onboarding.DismissedExtra(
                method = dismissedMethod.telemetryId,
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
        Pings.onboarding.submit()
    }

    /**
     * Records "onboarding_started" telemetry event.
     */
    fun onOnboardingStarted() {
        Onboarding.started.record(
            extra = Onboarding.StartedExtra(
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
    }

    /**
     * Records impression events for a given [OnboardingPageUiData.Type].
     * @param sequenceId The identifier of the onboarding sequence shown to the user.
     * @param pageType The page type for which the impression occurred.
     * @param sequencePosition The sequence position of the page for which the impression occurred.
     */
    @Suppress("LongMethod")
    fun onImpression(
        sequenceId: String,
        pageType: OnboardingPageUiData.Type,
        sequencePosition: String,
    ) {
        logger.debug(
            "Recording on impression event, sequenceId: $sequenceId, " +
                "pageType: $pageType, sequencePosition: $sequencePosition",
        )

        when (pageType) {
            OnboardingPageUiData.Type.DEFAULT_BROWSER -> {
                Onboarding.setToDefaultCard.record(
                    Onboarding.SetToDefaultCardExtra(
                        action = ACTION_IMPRESSION,
                        elementType = ET_ONBOARDING_CARD,
                        sequenceId = sequenceId,
                        sequencePosition = sequencePosition,
                        onboardingReason = onboardingReason.value,
                        installSource = installSource,
                    ),
                )
            }

            OnboardingPageUiData.Type.TERMS_OF_SERVICE -> {
                Onboarding.termsOfServiceCard.record(
                    Onboarding.TermsOfServiceCardExtra(
                        action = ACTION_IMPRESSION,
                        elementType = ET_ONBOARDING_CARD,
                        sequenceId = sequenceId,
                        sequencePosition = sequencePosition,
                        onboardingReason = onboardingReason.value,
                        installSource = installSource,
                    ),
                )
            }
        }
    }

    /**
     * Records set to default click event.
     * @param sequenceId The identifier of the onboarding sequence shown to the user.
     * @param sequencePosition The sequence position of the page for which the impression occurred.
     */
    fun onSetToDefaultClick(sequenceId: String, sequencePosition: String) {
        logger.debug(
            "Recording set to default click event, sequenceId: $sequenceId, " +
                "sequencePosition: $sequencePosition",
        )

        Onboarding.setToDefault.record(
            Onboarding.SetToDefaultExtra(
                action = ACTION_CLICK,
                elementType = ET_PRIMARY_BUTTON,
                sequenceId = sequenceId,
                sequencePosition = sequencePosition,
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
    }

    /**
     * Records skip set to default click event.
     * @param sequenceId The identifier of the onboarding sequence shown to the user.
     * @param sequencePosition The sequence position of the page for which the impression occurred.
     */
    fun onSkipSetToDefaultClick(sequenceId: String, sequencePosition: String) {
        Onboarding.skipDefault.record(
            Onboarding.SkipDefaultExtra(
                action = ACTION_CLICK,
                elementType = ET_SECONDARY_BUTTON,
                sequenceId = sequenceId,
                sequencePosition = sequencePosition,
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
    }

    /**
     * Records when the terms of service link is clicked.
     */
    fun onTermsOfServiceLinkClick() {
        Onboarding.termsOfServiceLinkClicked.record(
            extra = Onboarding.TermsOfServiceLinkClickedExtra(
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
    }

    /**
     * Records when the privacy notice link clicked.
     */
    fun onTermsOfServicePrivacyNoticeLinkClick() {
        Onboarding.termsOfServicePrivacyNoticeLinkClicked.record(
            extra = Onboarding.TermsOfServicePrivacyNoticeLinkClickedExtra(
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
    }

    /**
     * Records when the accept terms button clicked.
     */
    fun onTermsOfServiceManagerAcceptTermsButtonClick() {
        Onboarding.shown.record(
            extra = Onboarding.ShownExtra(
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
        Onboarding.termsOfServiceAccepted.record(
            extra = Onboarding.TermsOfServiceAcceptedExtra(
                onboardingReason = onboardingReason.value,
                installSource = installSource,
            ),
        )
        TermsOfUse.accepted.record(
            TermsOfUse.AcceptedExtra(
                surface = Surface.ONBOARDING.metricLabel,
                touVersion = TOU_VERSION,
            ),
        )
        TermsOfUse.version.set(TOU_VERSION.toLong())
        TermsOfUse.date.set()
    }

    /**
     * Sends the onboarding ping when the user navigates to the next onboarding page.
     */
    fun onNavigatedToNextPage() {
        Pings.onboarding.submit()
    }

    companion object {
        private const val ACTION_IMPRESSION = "impression"
        private const val ACTION_CLICK = "click"
        private const val ET_ONBOARDING_CARD = "onboarding_card"
        private const val ET_PRIMARY_BUTTON = "primary_button"
        private const val ET_SECONDARY_BUTTON = "secondary_button"
        const val ET_CARD_CLOSE_BUTTON = "card_close_button"
    }
}

/**
 * Enum representing the method used to dismiss the onboarding flow.
 * @property telemetryId The telemetry identifier.
 *
 * @see [Onboarding.DismissedExtra.method].
 */
enum class DismissedMethod(val telemetryId: String) {
    COMPLETED("completed"),
    SKIPPED("skipped"),
}
