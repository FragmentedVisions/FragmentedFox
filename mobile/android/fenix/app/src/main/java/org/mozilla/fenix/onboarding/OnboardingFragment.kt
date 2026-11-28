/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import mozilla.components.service.nimbus.evalJexlSafe
import mozilla.components.service.nimbus.messaging.use
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.utils.Browsers
import org.mozilla.fenix.BuildConfig
import org.mozilla.fenix.GleanMetrics.Pings
import org.mozilla.fenix.R
import org.mozilla.fenix.components.appstate.AppAction
import org.mozilla.fenix.components.appstate.SupportedMenuNotifications
import org.mozilla.fenix.components.initializeGlean
import org.mozilla.fenix.components.metrics.installSourcePackage
import org.mozilla.fenix.components.startMetricsIfEnabled
import org.mozilla.fenix.ext.application
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.ext.hideToolbar
import org.mozilla.fenix.ext.isLargeScreenSize
import org.mozilla.fenix.ext.nav
import org.mozilla.fenix.ext.openSetDefaultBrowserOption
import org.mozilla.fenix.ext.requireComponents
import org.mozilla.fenix.nimbus.FxNimbus
import org.mozilla.fenix.onboarding.view.OnboardingPageUiData
import org.mozilla.fenix.onboarding.view.OnboardingScreen
import org.mozilla.fenix.onboarding.view.sequencePosition
import org.mozilla.fenix.onboarding.view.telemetrySequenceId
import org.mozilla.fenix.onboarding.view.toPageUiData
import org.mozilla.fenix.settings.SupportUtils
import org.mozilla.fenix.theme.FirefoxTheme

/**
 * Fragment displaying the onboarding flow.
 */
class OnboardingFragment : Fragment() {
    private val logger = Logger("OnboardingFragment")

    private val termsOfServiceEventHandler by lazy {
        DefaultOnboardingTermsOfServiceEventHandler(
            telemetryRecorder = telemetryRecorder,
            openLink = this::launchSandboxCustomTab,
            settings = requireComponents.settings,
            startGlean = ::startGlean,
        )
    }

    private val pagesToDisplay by lazy {
        with(requireContext()) {
            pagesToDisplay(
                showDefaultBrowserPage = displayDefaultBrowserPage(this),
            ).toMutableList()
        }
    }

    private fun displayDefaultBrowserPage(context: Context): Boolean = isNotDefaultBrowser(context)

    private val telemetryRecorder by lazy {
        OnboardingTelemetryRecorder(
            onboardingReason = if (requireComponents.settings.enablePersistentOnboarding) {
                OnboardingReason.EXISTING_USER
            } else {
                OnboardingReason.NEW_USER
            },
            installSource = installSourcePackage(
                packageManager = requireContext().application.packageManager,
                packageName = requireContext().application.packageName,
            ),
        )
    }

    private val defaultBrowserPromptStorage by lazy { DefaultDefaultBrowserPromptStorage(requireContext()) }
    private val defaultBrowserPromptManager by lazy {
        DefaultBrowserPromptManager(
            storage = defaultBrowserPromptStorage,
            promptToSetAsDefaultBrowser = {
                requireContext().components.strictMode.allowViolation(StrictMode::allowThreadDiskReads) {
                    promptToSetAsDefaultBrowser()
                }
            },
        )
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (pagesToDisplay.isEmpty()) {
            // do not continue if there's no onboarding pages to display
            onFinish(null)
        }

        if (!isLargeScreenSize()) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // We want the prompt to be displayed once per onboarding opening.
        // In case the host got recreated, we don't reset the flag.
        if (savedInstanceState == null) {
            defaultBrowserPromptStorage.promptToSetAsDefaultBrowserDisplayedInOnboarding = false
        }

        telemetryRecorder.onOnboardingStarted()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        FirefoxTheme {
            ScreenContent()
        }
    }

    override fun onResume() {
        super.onResume()
        hideToolbar()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!isLargeScreenSize()) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isLargeScreenSize()) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    @Composable
    @Suppress("LongMethod")
    private fun ScreenContent() {
        OnboardingScreen(
            pagesToDisplay = pagesToDisplay,
            onMakeFirefoxDefaultClick = {
                promptToSetAsDefaultBrowser()
            },
            onSkipDefaultClick = {
                telemetryRecorder.onSkipSetToDefaultClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.DEFAULT_BROWSER),
                )
            },
            onFinish = {
                onFinish(it)
            },
            onImpression = {
                telemetryRecorder.onImpression(
                    sequenceId = pagesToDisplay.telemetrySequenceId(),
                    pageType = it.type,
                    sequencePosition = pagesToDisplay.sequencePosition(it.type),
                )

                defaultBrowserPromptManager.maybePromptToSetAsDefaultBrowser(
                    pagesToDisplay = pagesToDisplay,
                    currentCard = it,
                )
            },
            termsOfServiceEventHandler = termsOfServiceEventHandler,
            onNavigateToNextPage = {
                telemetryRecorder.onNavigatedToNextPage()
            },
        )
    }

    private fun startGlean() {
        val settings = requireComponents.settings
        viewLifecycleOwner.lifecycleScope.launch {
            initializeGlean(
                requireContext().applicationContext,
                logger,
                settings.isTelemetryEnabled,
                requireComponents.core.client,
            )
        }

        if (BuildConfig.TELEMETRY && !settings.isTelemetryEnabled) {
            Pings.onboardingOptOut.setEnabled(true)
            Pings.onboardingOptOut.submit()
        }

        startMetricsIfEnabled(
            logger = logger,
            analytics = requireComponents.analytics,
            isTelemetryEnabled = settings.isTelemetryEnabled,
            isDailyUsagePingEnabled = settings.isDailyUsagePingEnabled,
        )
    }

    private fun onFinish(onboardingPageUiData: OnboardingPageUiData?) {
        // onboarding page UI data can be null if there was no pages to display
        if (onboardingPageUiData != null) {
            val sequenceId = pagesToDisplay.telemetrySequenceId()
            val sequencePosition = pagesToDisplay.sequencePosition(onboardingPageUiData.type)

            telemetryRecorder.onOnboardingComplete(
                sequenceId = sequenceId,
                sequencePosition = sequencePosition,
            )
        }

        requireComponents.fenixOnboarding.finish()

        val settings = requireComponents.settings
        settings.onboardingCompletedTimestamp = System.currentTimeMillis()

        // Telemetry and daily usage ping get enabled after ToU acceptance.
        startMetricsIfEnabled(
            logger = logger,
            analytics = requireComponents.analytics,
            isTelemetryEnabled = false,
            isDailyUsagePingEnabled = false,
        )

        findNavController().nav(
            id = R.id.onboardingFragment,
            directions = OnboardingFragmentDirections.actionHome(),
        )

        maybeAddMenuNotification()
    }

    private fun isNotDefaultBrowser(context: Context) =
        !Browsers.isDefaultBrowser(context)

    private fun pagesToDisplay(
        showDefaultBrowserPage: Boolean,
    ): List<OnboardingPageUiData> {
        val jexlConditions = FxNimbus.features.junoOnboarding.value().conditions
        val jexlHelper = requireContext().components.nimbus.createJexlHelper()

        return jexlHelper.use {
            FxNimbus.features.junoOnboarding.value().cards.values.toPageUiData(
                showDefaultBrowserPage,
                jexlConditions,
            ) { condition -> jexlHelper.evalJexlSafe(condition) }
        }
    }

    private fun promptToSetAsDefaultBrowser() {
        activity?.openSetDefaultBrowserOption(useCustomTab = true)
        requireComponents.settings.coldStartsBetweenSetAsDefaultPrompts = 0
        requireComponents.settings.lastSetAsDefaultPromptShownTimeInMillis = System.currentTimeMillis()
        telemetryRecorder.onSetToDefaultClick(
            sequenceId = pagesToDisplay.telemetrySequenceId(),
            sequencePosition = pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.DEFAULT_BROWSER),
        )
    }

    private fun launchSandboxCustomTab(url: String) =
        SupportUtils.launchSandboxCustomTab(requireContext(), url)

    private fun maybeAddMenuNotification() {
        with(requireContext()) {
            if (shouldAddMenuNotification()) {
                requireComponents.appStore.dispatch(
                    AppAction.MenuNotification.AddMenuNotification(
                        SupportedMenuNotifications.NotDefaultBrowser,
                    ),
                )
            }
        }
    }

    private fun shouldAddMenuNotification() =
        with(requireComponents) { !settings.isDefaultBrowser && settings.shouldShowMenuBanner }
}
