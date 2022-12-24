package com.kazakago.swr.compose.validation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
public class FocusThrottleIntervalOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun focusThrottleInterval5Seconds() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateOnFocus = true
                focusThrottleInterval = 5.seconds
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false, true, false)
    }

    @Test
    public fun focusThrottleInterval10Seconds() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateOnFocus = true
                focusThrottleInterval = 10.seconds
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)
    }

    @Test
    public fun focusThrottleInterval0Seconds() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateOnFocus = true
                focusThrottleInterval = 0.seconds
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched", "fetched", "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false, true, false, true, false)

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.mainClock.advanceTimeBy(2100)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched", "fetched", "fetched", "fetched", "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false, false, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false, true, false, true, false, true, false)
    }
}
