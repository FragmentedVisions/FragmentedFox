/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.datachoices

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mozilla.components.compose.base.LinkText
import mozilla.components.compose.base.LinkTextState
import mozilla.components.compose.base.annotation.FlexibleWindowPreview
import mozilla.components.lib.crash.store.CrashReportOption
import mozilla.components.lib.state.ext.observeAsComposableState
import org.mozilla.fenix.R
import org.mozilla.fenix.compose.list.RadioButtonListItem
import org.mozilla.fenix.compose.settings.SettingsSectionHeader
import org.mozilla.fenix.settings.settingssearch.PreferenceFileInformation
import org.mozilla.fenix.settings.settingssearch.SettingsSearchItem
import org.mozilla.fenix.settings.settingssearch.SettingsSearchProvider
import org.mozilla.fenix.theme.FirefoxTheme
import org.mozilla.fenix.theme.PreviewThemeProvider
import org.mozilla.fenix.theme.Theme

private enum class DataChoicesSectionKey {
    CRASH_REPORTS,
}

/**
 * Composable function that renders the Data Choices settings screen.
 *
 * This screen allows the user to view and modify their crash reporting preferences.
 *
 * @param store The [DataChoicesStore] used to manage and access the [DataChoicesState]
 **/
@Composable
internal fun DataChoicesScreen(
    store: DataChoicesStore,
) {
    val state by store.observeAsComposableState { it }
    val onCrashOptionSelected: (CrashReportOption) -> Unit = { newValue ->
        store.dispatch(ChoiceAction.ReportOptionClicked(newValue))
    }
    val onScrolledToItem = { store.dispatch(ChoiceAction.ScrolledToItem) }
    val learnMoreCrashReport: () -> Unit = { store.dispatch(LearnMore.CrashLearnMoreClicked) }

    Surface {
        DataChoicesUi(
            state = state,
            onCrashOptionSelected = onCrashOptionSelected,
            onScrolledToItem = onScrolledToItem,
            learnMoreCrashReport = learnMoreCrashReport,
        )
    }
}

@Composable
internal fun DataChoicesUi(
    state: DataChoicesState,
    onCrashOptionSelected: (CrashReportOption) -> Unit,
    onScrolledToItem: () -> Unit,
    learnMoreCrashReport: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val items = buildDataChoicesItems(state)

    LaunchedEffect(state.itemToScrollTo) {
        if (!state.itemToScrollTo.isNullOrBlank()) {
            val key = DataChoicesSectionKey.valueOf(state.itemToScrollTo)
            val index = items.indexOf(key)
            if (index != -1) {
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(index)
                    onScrolledToItem()
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = lazyListState,
    ) {
        items(
            items = items,
            key = { it },
        ) { section ->
            when (section) {
                DataChoicesSectionKey.CRASH_REPORTS -> CrashReportsSection(
                    learnMoreText = stringResource(R.string.preferences_crashes_learn_more),
                    selectedOption = state.selectedCrashOption,
                    onOptionSelected = onCrashOptionSelected,
                    onLearnMoreClicked = learnMoreCrashReport,
                )
            }
            if (section != items.last()) {
                HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 24.dp))
            }
        }
    }
}

@Composable
private fun buildDataChoicesItems(state: DataChoicesState): List<DataChoicesSectionKey> {
    return remember {
        buildList {
            add(DataChoicesSectionKey.CRASH_REPORTS)
        }
    }
}

/**
 * Composable section for configuring crash reporting preferences.
 *
 * @param learnMoreText The text to display for the "Learn More" link.
 * @param selectedOption The currently selected crash reporting option.
 * @param onOptionSelected Callback invoked when the user selects a different crash report option.
 * @param onLearnMoreClicked Callback invoked when the "Learn More" link is clicked.
 * */
@Composable
private fun CrashReportsSection(
    learnMoreText: String,
    selectedOption: CrashReportOption = CrashReportOption.Ask,
    onOptionSelected: (CrashReportOption) -> Unit,
    onLearnMoreClicked: () -> Unit,
) {
    Column {
        SettingsSectionHeader(
            text = stringResource(R.string.crash_reports_data_category),
            modifier = Modifier.padding(horizontal = FirefoxTheme.layout.space.dynamic200),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.crash_reporting_description),
            modifier = Modifier.padding(horizontal = FirefoxTheme.layout.space.dynamic200),
            style = FirefoxTheme.typography.body2,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CrashReportOption.entries.forEach { crashReportOption ->
                RadioButtonListItem(
                    label = stringResource(crashReportOption.labelId),
                    selected = selectedOption == crashReportOption,
                    modifier = Modifier
                        .semantics {
                            testTag = "data.collection.$crashReportOption.option"
                            testTagsAsResourceId = true
                        },
                    maxLabelLines = 1,
                    maxDescriptionLines = 1,
                    onClick = { onOptionSelected(crashReportOption) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LearnMoreLink(onLearnMoreClicked, learnMoreText)
    }
}

/**
 * Composable that displays a "Learn More" text link.
 *
 * @param onLearnMoreClicked Callback invoked when the user clicks the link.
 * @param learnMoreText The text to display as the link.
 */
@Composable
private fun LearnMoreLink(onLearnMoreClicked: () -> Unit, learnMoreText: String) {
    val learnMoreState = LinkTextState(
        text = learnMoreText,
        url = "",
        onClick = {
            onLearnMoreClicked()
        },
    )

    Column(
        modifier = Modifier
            .clickable(onClick = { onLearnMoreClicked() })
            .fillMaxWidth()
            .padding(horizontal = FirefoxTheme.layout.space.dynamic200),
    ) {
        LinkText(
            text = learnMoreText,
            linkTextStates = listOf(learnMoreState),
            linkTextDecoration = TextDecoration.Underline,
        )
    }
}

@FlexibleWindowPreview
@Composable
private fun DataChoicesPreview(
    @PreviewParameter(PreviewThemeProvider::class) theme: Theme,
) {
    FirefoxTheme(theme) {
        DataChoicesScreen(
            store = DataChoicesStore(
                initialState = DataChoicesState(),
            ),
        )
    }
}

/**
 * Provides [SettingsSearchItem]s for the Data Choices settings screen for use in settings search.
 */
object DataChoicesSearchProvider : SettingsSearchProvider {
    private val preferenceFileInformation = PreferenceFileInformation.DataChoicesPreferences

    override fun getSearchItems(context: Context): List<SettingsSearchItem> {
        return buildList {
            add(
                buildSearchItem(
                    context = context,
                    titleRes = R.string.crash_reports_data_category,
                    summaryRes = R.string.crash_reporting_description,
                    key = DataChoicesSectionKey.CRASH_REPORTS,
                ),
            )
        }
    }

    private fun buildSearchItem(
        context: Context,
        @StringRes titleRes: Int,
        @StringRes summaryRes: Int?,
        key: DataChoicesSectionKey,
    ) = SettingsSearchItem(
        title = context.getString(titleRes),
        summary = when (summaryRes) {
            null -> ""
            else -> context.getString(summaryRes)
        },
        preferenceKey = key.name,
        categoryHeader = context.getString(preferenceFileInformation.categoryHeaderResourceId),
        preferenceFileInformation = preferenceFileInformation,
    )
}
