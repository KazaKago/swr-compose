package com.kazakago.swr.compose.validation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class RevalidateOnMountOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun noRevalidateOnMount() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateOnMount = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null)
        stateList.map { it.error } shouldBe listOf(null)
        stateList.map { it.isLoading } shouldBe listOf(true)
        stateList.map { it.isValidating } shouldBe listOf(false)
    }

    @Test
    public fun noRevalidateOnMountWithMutate() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRState<String, String>>()
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                revalidateOnMount = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null)
        stateList.map { it.error } shouldBe listOf(null)
        stateList.map { it.isLoading } shouldBe listOf(true)
        stateList.map { it.isValidating } shouldBe listOf(false)

        scope.launch {
            stateList.last().mutate()
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        stateList.map { it.data } shouldBe listOf(null, null, "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(true, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)
    }
}
