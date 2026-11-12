/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.browser.state.engine

import kotlinx.coroutines.test.runTest
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession
import mozilla.components.support.test.mock
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify

class EngineMiddlewareTest {

    @Test
    fun `Dispatching CreateEngineSessionAction multiple times should only create one engine session`() = runTest {
        val session: EngineSession = mock()
        val engine: Engine = mock()
        Mockito.doReturn(session).`when`(engine).createSession(false, null)

        val store = BrowserStore(
            initialState = BrowserState(
                tabs = listOf(
                    createTab("https://www.mozilla.org", id = "mozilla"),
                ),
            ),
            middleware = EngineMiddleware.create(engine, this),
        )

        store.dispatch(
            EngineAction.CreateEngineSessionAction("mozilla"),
        )

        store.dispatch(
            EngineAction.CreateEngineSessionAction("mozilla"),
        )

        testScheduler.advanceUntilIdle()

        verify(engine, Mockito.times(1)).createSession(false, null)
    }
}
