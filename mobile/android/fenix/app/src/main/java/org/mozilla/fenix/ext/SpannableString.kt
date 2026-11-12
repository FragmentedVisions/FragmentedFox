/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ext

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.AttrRes
import mozilla.components.support.ktx.android.content.getColorFromAttr

fun SpannableString.setTextColor(
    context: Context,
    @AttrRes colorResId: Int,
) =
    this.setSpan(
        ForegroundColorSpan(context.getColorFromAttr(colorResId)),
        0,
        this.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
