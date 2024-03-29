package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.useSWR
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
public class LoadingTimeoutOptionTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun focusThrottleInterval3Seconds() {
        val key = object {}.javaClass.enclosingMethod?.name
        val keyList = mutableListOf<String>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(10.seconds)
                "fetched"
            }) {
                loadingTimeout = 3.seconds
                onLoadingSlow = { key, _ ->
                    keyList.add(key)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2000)
        keyList shouldBe emptyList()
        composeTestRule.mainClock.advanceTimeBy(2000)
        keyList shouldBe listOf(key)
    }

    @Test
    public fun focusThrottleInterval5Seconds() {
        val key = object {}.javaClass.enclosingMethod?.name
        val keyList = mutableListOf<String>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(10.seconds)
                "fetched"
            }) {
                loadingTimeout = 5.seconds
                onLoadingSlow = { key, _ ->
                    keyList.add(key)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2000)
        keyList shouldBe emptyList()
        composeTestRule.mainClock.advanceTimeBy(2000)
        keyList shouldBe emptyList()
    }

    @Test
    public fun focusThrottleInterval0Seconds() {
        val key = object {}.javaClass.enclosingMethod?.name
        val keyList = mutableListOf<String>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(10.seconds)
                "fetched"
            }) {
                loadingTimeout = 0.seconds
                onLoadingSlow = { key, _ ->
                    keyList.add(key)
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2000)
        keyList shouldBe listOf(key)
        composeTestRule.mainClock.advanceTimeBy(2000)
        keyList shouldBe listOf(key)
    }
}
