package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
public class OnErrorOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun withOnError() {
        val key = Random.nextInt().toString()
        val onErrorList = mutableListOf<Pair<Throwable, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                onSuccess = { _, _, _ ->
                    fail("Must not reach here")
                }
                onError = { error, key, _ ->
                    onErrorList += error to key
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(3000)
        assertEquals(listOf<Pair<Throwable, String?>>(DummyException1 to key), onErrorList)
    }
}
