package com.kazakago.swr.compose.validation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.DummyException2
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
public class ErrorRetryCountOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun errorRetryCountNull() {
        val key = object {}.javaClass.enclosingMethod?.name
        val errorRetryCountList = mutableListOf<Int?>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                errorRetryCount = null
                onErrorRetry = { _, _, config, _, _ ->
                    errorRetryCountList += config.errorRetryCount
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        errorRetryCountList shouldBe listOf(null)
    }

    @Test
    public fun errorRetryCount3() {
        val key = object {}.javaClass.enclosingMethod?.name
        val errorRetryCountList = mutableListOf<Int?>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                errorRetryCount = 3
                onErrorRetry = { _, _, config, _, _ ->
                    errorRetryCountList += config.errorRetryCount
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        errorRetryCountList shouldBe listOf(3)
    }
}
