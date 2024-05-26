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
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
public class OnLoadingSlowOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun onLoadingSlow() {
        val key = Random.nextInt().toString()
        val keyList = mutableListOf<String>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(5.seconds)
                "fetched"
            }) {
                onLoadingSlow = { key, _ ->
                    keyList.add(key)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(5000)
        assertEquals(listOf(key), keyList)
    }

    @Test
    public fun onLoadingSlow2() {
        val key = Random.nextInt().toString()
        val keyList = mutableListOf<String>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(2.seconds)
                "fetched"
            }) {
                onLoadingSlow = { key, _ ->
                    keyList.add(key)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(5000)
        assertEquals(emptyList(), keyList)
    }
}
