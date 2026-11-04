/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.settings.ai

import mozilla.components.concept.ai.controls.AIFeatureMetadata
import org.junit.Assert.assertNull
import org.junit.Test

class DestinationTest {

    @Test
    fun `unknown feature id returns null destination`() {
        val metadata = object : AIFeatureMetadata {
            override val id = AIFeatureMetadata.FeatureId("unknown.feature")
            override val description = AIFeatureMetadata.Description(0, 0, 0)
        }

        assertNull(metadata.destination)
    }
}
