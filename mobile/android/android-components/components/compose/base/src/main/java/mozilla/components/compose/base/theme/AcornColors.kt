/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("MagicNumber")

package mozilla.components.compose.base.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import mozilla.components.ui.colors.FragmentedFoxColors
import mozilla.components.ui.colors.NovaColors
import mozilla.components.ui.colors.PhotonColors

/**
 * A custom Color Palette for Mozilla Firefox for Android (Fenix).
 */
@Suppress("LongParameterList")
@Stable
class AcornColors(
    layerGradientStart: Color,
    layerGradientEnd: Color,
    formDefault: Color,
    textOnColorPrimary: Color,
    iconOnColor: Color,
    information: Color,
    onInformation: Color,
    informationContainer: Color,
    onInformationContainer: Color,
    success: Color,
    onSuccess: Color,
    warning: Color,
    onWarning: Color,
    warningContainer: Color,
    onWarningContainer: Color,
    surfaceDimVariant: Color,
    autofillText: Color,
    selectedText: Color,
    iconPrivate: Color,
) {
    // Tooltip
    var layerGradientStart by mutableStateOf(layerGradientStart)
        private set

    // Tooltip
    var layerGradientEnd by mutableStateOf(layerGradientEnd)
        private set

    // Checkbox default, Radio button default
    var formDefault by mutableStateOf(formDefault)
        private set

    // Text

    // Text Inverted/On Color
    var textOnColorPrimary by mutableStateOf(textOnColorPrimary)
        private set

    // Icon

    // Icon inverted (on color)
    var iconOnColor by mutableStateOf(iconOnColor)
        private set

    /*
     * M3 color scheme extensions that do not have a mapped value from Acorn
     */

    /**
     * Attention-grabbing color against surface for fills, icons, and text,
     * indicating neutral information.
     */
    internal var information by mutableStateOf(information)
        private set

    /**
     * Text and icons against information.
     */
    internal var onInformation by mutableStateOf(onInformation)
        private set

    /**
     * Less prominent fill color against surface, for neutral information.
     */
    internal var informationContainer by mutableStateOf(informationContainer)
        private set

    /**
     * Text and icons against information container.
     */
    internal var onInformationContainer by mutableStateOf(onInformationContainer)
        private set

    /**
     * Attention-grabbing color against surface for fills, icons, and text,
     * indicating successful information
     */
    internal var success by mutableStateOf(success)
        private set

    /**
     * Text and icons against success.
     */
    internal var onSuccess by mutableStateOf(onSuccess)
        private set

    /**
     * Attention-grabbing color against surface for fills, icons, and text, indicating
     * warning information.
     */
    internal var warning by mutableStateOf(warning)
        private set

    /**
     * Text and icons against warning.
     */
    internal var onWarning by mutableStateOf(onWarning)
        private set

    /**
     * Less prominent fill color against surface, for warning information.
     */
    internal var warningContainer by mutableStateOf(warningContainer)
        private set

    /**
     * Text and icons against warning container.
     */
    internal var onWarningContainer by mutableStateOf(onWarningContainer)
        private set

    /**
     * Slightly dimmer surface color in light theme.
     */
    internal var surfaceDimVariant by mutableStateOf(surfaceDimVariant)
        private set

    /**
     * Highlighted autofill text color.
     */
    internal var autofillText by mutableStateOf(autofillText)
        private set

    /**
     * Highlighted selected text color.
     */
    internal var selectedText by mutableStateOf(selectedText)
        private set

    /**
     * Private mode icon.
     */
    internal var iconPrivate by mutableStateOf(iconPrivate)
        private set

    /**
     * Updates the existing colors with the provided [AcornColors].
     */
    fun update(other: AcornColors) {
        layerGradientStart = other.layerGradientStart
        layerGradientEnd = other.layerGradientEnd
        formDefault = other.formDefault
        textOnColorPrimary = other.textOnColorPrimary
        iconOnColor = other.iconOnColor
        information = other.information
        onInformation = other.onInformation
        informationContainer = other.informationContainer
        onInformationContainer = other.onInformationContainer
        success = other.success
        onSuccess = other.onSuccess
        warning = other.warning
        onWarning = other.onWarning
        warningContainer = other.warningContainer
        onWarningContainer = other.onWarningContainer
        surfaceDimVariant = other.surfaceDimVariant
        autofillText = other.autofillText
        selectedText = other.selectedText
        iconPrivate = other.iconPrivate
    }

    /**
     * Return a copy of this [AcornColors] and optionally overriding any of the provided values.
     */
    fun copy(
        layerGradientStart: Color = this.layerGradientStart,
        layerGradientEnd: Color = this.layerGradientEnd,
        formDefault: Color = this.formDefault,
        textOnColorPrimary: Color = this.textOnColorPrimary,
        iconOnColor: Color = this.iconOnColor,
        information: Color = this.information,
        onInformation: Color = this.onInformation,
        informationContainer: Color = this.informationContainer,
        onInformationContainer: Color = this.onInformationContainer,
        success: Color = this.success,
        onSuccess: Color = this.onSuccess,
        warning: Color = this.warning,
        onWarning: Color = this.onWarning,
        warningContainer: Color = this.warningContainer,
        onWarningContainer: Color = this.onWarningContainer,
        surfaceDimVariant: Color = this.surfaceDimVariant,
        autofillText: Color = this.autofillText,
        selectedText: Color = this.selectedText,
        iconPrivate: Color = this.iconPrivate,
    ): AcornColors = AcornColors(
        layerGradientStart = layerGradientStart,
        layerGradientEnd = layerGradientEnd,
        formDefault = formDefault,
        textOnColorPrimary = textOnColorPrimary,
        iconOnColor = iconOnColor,
        information = information,
        onInformation = onInformation,
        informationContainer = informationContainer,
        onInformationContainer = onInformationContainer,
        success = success,
        onSuccess = onSuccess,
        warning = warning,
        onWarning = onWarning,
        warningContainer = warningContainer,
        onWarningContainer = onWarningContainer,
        surfaceDimVariant = surfaceDimVariant,
        autofillText = autofillText,
        selectedText = selectedText,
        iconPrivate = iconPrivate,
    )
}

val darkColorPalette = AcornColors(
    layerGradientStart = FragmentedFoxColors.RedDeep,
    layerGradientEnd = FragmentedFoxColors.SignalRed,
    formDefault = PhotonColors.LightGrey05,
    textOnColorPrimary = PhotonColors.LightGrey05,
    iconOnColor = PhotonColors.LightGrey05,
    information = FragmentedFoxColors.Lavender,
    onInformation = FragmentedFoxColors.Void,
    informationContainer = FragmentedFoxColors.LavenderInk,
    onInformationContainer = FragmentedFoxColors.SoftWhite,
    success = FragmentedFoxColors.TerminalGreen,
    onSuccess = FragmentedFoxColors.Void,
    warning = FragmentedFoxColors.BrightRed,
    onWarning = FragmentedFoxColors.Void,
    warningContainer = FragmentedFoxColors.RedDark,
    onWarningContainer = FragmentedFoxColors.SoftWhite,
    surfaceDimVariant = FragmentedFoxColors.Charcoal0,
    autofillText = FragmentedFoxColors.LavenderA55,
    selectedText = FragmentedFoxColors.AshA80,
    iconPrivate = FragmentedFoxColors.Lavender,
)

val lightColorPalette = AcornColors(
    layerGradientStart = FragmentedFoxColors.RedDeep,
    layerGradientEnd = FragmentedFoxColors.SignalRed,
    formDefault = PhotonColors.DarkGrey90,
    textOnColorPrimary = PhotonColors.LightGrey05,
    iconOnColor = PhotonColors.LightGrey05,
    information = FragmentedFoxColors.LavenderDeep,
    onInformation = FragmentedFoxColors.White,
    informationContainer = FragmentedFoxColors.LavenderTint,
    onInformationContainer = FragmentedFoxColors.LavenderInk,
    success = FragmentedFoxColors.GreenDeep,
    onSuccess = FragmentedFoxColors.White,
    warning = FragmentedFoxColors.SignalRed,
    onWarning = FragmentedFoxColors.White,
    warningContainer = FragmentedFoxColors.RedTint,
    onWarningContainer = FragmentedFoxColors.RedDark,
    surfaceDimVariant = FragmentedFoxColors.Grey200,
    autofillText = FragmentedFoxColors.Lavender,
    selectedText = FragmentedFoxColors.Ash,
    iconPrivate = FragmentedFoxColors.Lavender,
)

val privateColorPalette = darkColorPalette.copy(
    surfaceDimVariant = FragmentedFoxColors.PrivateBg,
    selectedText = FragmentedFoxColors.AshA80,
)

/**
 * Returns a dark Material color scheme mapped from Acorn.
 */
fun acornDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = FragmentedFoxColors.BrightRed,
    onPrimary = FragmentedFoxColors.Void,
    primaryContainer = FragmentedFoxColors.SignalRed,
    onPrimaryContainer = FragmentedFoxColors.SoftWhite,
    inversePrimary = FragmentedFoxColors.SignalRed,
    secondary = FragmentedFoxColors.Ash,
    onSecondary = FragmentedFoxColors.Void,
    secondaryContainer = FragmentedFoxColors.Line,
    onSecondaryContainer = FragmentedFoxColors.SoftWhite,
    tertiary = FragmentedFoxColors.Lavender,
    onTertiary = FragmentedFoxColors.Void,
    tertiaryContainer = FragmentedFoxColors.LavenderInk,
    onTertiaryContainer = FragmentedFoxColors.SoftWhite,
    background = FragmentedFoxColors.Void,
    onBackground = FragmentedFoxColors.SoftWhite,
    surface = FragmentedFoxColors.Charcoal,
    onSurface = FragmentedFoxColors.SoftWhite,
    surfaceVariant = FragmentedFoxColors.Charcoal2,
    onSurfaceVariant = FragmentedFoxColors.Ash,
    surfaceTint = NovaColors.Gray50,
    inverseSurface = FragmentedFoxColors.SoftWhite,
    inverseOnSurface = FragmentedFoxColors.Void,
    error = FragmentedFoxColors.BrightRed,
    onError = FragmentedFoxColors.Void,
    errorContainer = FragmentedFoxColors.RedDark,
    onErrorContainer = FragmentedFoxColors.SoftWhite,
    outline = FragmentedFoxColors.Grey600,
    outlineVariant = FragmentedFoxColors.Line,
    scrim = NovaColors.BlackA50,
    surfaceBright = FragmentedFoxColors.Charcoal3,
    surfaceDim = FragmentedFoxColors.Void,
    surfaceContainer = FragmentedFoxColors.Charcoal,
    surfaceContainerHigh = FragmentedFoxColors.Charcoal2,
    surfaceContainerHighest = FragmentedFoxColors.Charcoal3,
    surfaceContainerLow = FragmentedFoxColors.Charcoal0,
    surfaceContainerLowest = FragmentedFoxColors.Void,
)

/**
 * Returns a light Material color scheme mapped from Acorn.
 */
fun acornLightColorScheme(): ColorScheme = lightColorScheme(
    primary = FragmentedFoxColors.SignalRed,
    onPrimary = FragmentedFoxColors.White,
    primaryContainer = FragmentedFoxColors.RedTint,
    onPrimaryContainer = FragmentedFoxColors.RedDark,
    inversePrimary = FragmentedFoxColors.BrightRed,
    secondary = FragmentedFoxColors.AshDark,
    onSecondary = FragmentedFoxColors.White,
    secondaryContainer = FragmentedFoxColors.Grey200,
    onSecondaryContainer = FragmentedFoxColors.Charcoal,
    tertiary = FragmentedFoxColors.LavenderDeep,
    onTertiary = FragmentedFoxColors.White,
    tertiaryContainer = FragmentedFoxColors.LavenderTint,
    onTertiaryContainer = FragmentedFoxColors.LavenderInk,
    background = FragmentedFoxColors.SoftWhite,
    onBackground = FragmentedFoxColors.Charcoal,
    surface = FragmentedFoxColors.SoftWhite,
    onSurface = FragmentedFoxColors.Charcoal,
    surfaceVariant = FragmentedFoxColors.Grey200,
    onSurfaceVariant = FragmentedFoxColors.AshDark,
    surfaceTint = NovaColors.Gray30,
    inverseSurface = FragmentedFoxColors.Charcoal2,
    inverseOnSurface = FragmentedFoxColors.SoftWhite,
    error = FragmentedFoxColors.RedDeep,
    onError = FragmentedFoxColors.White,
    errorContainer = FragmentedFoxColors.RedTint,
    onErrorContainer = FragmentedFoxColors.RedDark,
    outline = FragmentedFoxColors.Grey500,
    outlineVariant = FragmentedFoxColors.Grey300,
    scrim = NovaColors.BlackA50,
    surfaceBright = FragmentedFoxColors.White,
    surfaceDim = FragmentedFoxColors.Grey200,
    surfaceContainer = FragmentedFoxColors.SoftWhite,
    surfaceContainerHigh = FragmentedFoxColors.Grey100,
    surfaceContainerHighest = FragmentedFoxColors.Grey200,
    surfaceContainerLow = FragmentedFoxColors.Grey050,
    surfaceContainerLowest = FragmentedFoxColors.White,
)

/**
 * Returns a private Material color scheme mapped from Acorn.
 */
fun acornPrivateColorScheme(): ColorScheme = darkColorScheme(
    primary = FragmentedFoxColors.Lavender,
    onPrimary = FragmentedFoxColors.Void,
    primaryContainer = FragmentedFoxColors.LavenderDark,
    onPrimaryContainer = FragmentedFoxColors.SoftWhite,
    inversePrimary = FragmentedFoxColors.LavenderDark,
    secondary = FragmentedFoxColors.Ash,
    onSecondary = FragmentedFoxColors.Void,
    secondaryContainer = FragmentedFoxColors.PrivateLine,
    onSecondaryContainer = FragmentedFoxColors.SoftWhite,
    tertiary = FragmentedFoxColors.LavenderLight,
    onTertiary = FragmentedFoxColors.Void,
    tertiaryContainer = FragmentedFoxColors.LavenderInk,
    onTertiaryContainer = FragmentedFoxColors.SoftWhite,
    background = FragmentedFoxColors.PrivateBg,
    onBackground = FragmentedFoxColors.SoftWhite,
    surface = FragmentedFoxColors.PrivateSurface,
    onSurface = FragmentedFoxColors.SoftWhite,
    surfaceVariant = FragmentedFoxColors.PrivateVariant,
    onSurfaceVariant = FragmentedFoxColors.Ash,
    surfaceTint = NovaColors.VioletDesaturated60,
    inverseSurface = FragmentedFoxColors.SoftWhite,
    inverseOnSurface = FragmentedFoxColors.Void,
    error = FragmentedFoxColors.BrightRed,
    onError = FragmentedFoxColors.Void,
    errorContainer = FragmentedFoxColors.RedDark,
    onErrorContainer = FragmentedFoxColors.SoftWhite,
    outline = FragmentedFoxColors.Grey600,
    outlineVariant = FragmentedFoxColors.PrivateLine,
    scrim = NovaColors.BlackA50,
    surfaceBright = FragmentedFoxColors.PrivateVariant,
    surfaceDim = FragmentedFoxColors.Void,
    surfaceContainer = FragmentedFoxColors.PrivateSurface,
    surfaceContainerHigh = FragmentedFoxColors.PrivateVariant,
    surfaceContainerHighest = FragmentedFoxColors.PrivateLine,
    surfaceContainerLow = FragmentedFoxColors.PrivateBg,
    surfaceContainerLowest = Color(0xFF0D0321),
)

// M3 color scheme extensions

/**
 * @see AcornColors.information
 */
val ColorScheme.information: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.information

/**
 * @see AcornColors.onInformation
 */
val ColorScheme.onInformation: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.onInformation

/**
 * @see AcornColors.informationContainer
 */
val ColorScheme.informationContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.informationContainer

/**
 * @see AcornColors.onInformationContainer
 */
val ColorScheme.onInformationContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.onInformationContainer

/**
 * @see AcornColors.success
 */
val ColorScheme.success: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.success

/**
 * @see AcornColors.onSuccess
 */
val ColorScheme.onSuccess: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.onSuccess

/**
 * @see AcornColors.warning
 */
val ColorScheme.warning: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.warning

/**
 * @see AcornColors.onWarning
 */
val ColorScheme.onWarning: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.onWarning

/**
 * @see AcornColors.warningContainer
 */
val ColorScheme.warningContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.warningContainer

/**
 * @see AcornColors.onWarningContainer
 */
val ColorScheme.onWarningContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.onWarningContainer

/**
 * @see AcornColors.surfaceDimVariant
 */
val ColorScheme.surfaceDimVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.surfaceDimVariant

/**
 * @see AcornColors.autofillText
 */
val ColorScheme.autofillText: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.autofillText

/**
 * @see AcornColors.selectedText
 */
val ColorScheme.selectedText: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.selectedText

/**
 * @see AcornColors.iconPrivate
 */
val ColorScheme.iconPrivate: Color
    @Composable
    @ReadOnlyComposable
    get() = AcornTheme.colors.iconPrivate
