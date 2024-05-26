package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
public class OnSuccessOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun withOnSuccess() {
        val key = Random.nextInt().toString()
        val onSuccessList = mutableListOf<Pair<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                onSuccess = { data, key, _ ->
                    onSuccessList += data to key
                }
                onError = { _, _, _ ->
                    fail("Must not reach here")
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(3000)
        assertEquals(listOf("fetched" to key), onSuccessList)
    }
}
