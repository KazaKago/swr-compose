package com.kazakago.swr.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.state.SWRState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
public class MutationOptionsTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun mutationWithNoRevalidate() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched_1" }
        lateinit var scope: CoroutineScope
        lateinit var state: SWRState<String, String>
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            }) {
                dedupingInterval = 0.seconds
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1500)

        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe false

        result = { "fetched_2" }
        scope.launch {
            state.mutate(data = {
                delay(1000)
                "mutated"
            }) {
                revalidate = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "mutated"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "mutated"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "mutated"
        state.error shouldBe null
        state.isValidating shouldBe false
    }

    @Test
    public fun mutationWithOptimisticData() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched_1" }
        lateinit var scope: CoroutineScope
        lateinit var state: SWRState<String, String>
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            }) {
                dedupingInterval = 0.seconds
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1500)

        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe false

        result = { "fetched_2" }
        scope.launch {
            state.mutate(data = {
                delay(1000)
                "mutated"
            }) {
                optimisticData = "optimisticData"
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "optimisticData"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "mutated"
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "mutated"
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_2"
        state.error shouldBe null
        state.isValidating shouldBe false
    }

    @Test
    public fun mutationWithNoPopulateCache() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched_1" }
        lateinit var scope: CoroutineScope
        lateinit var state: SWRState<String, String>
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            }) {
                dedupingInterval = 0.seconds
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1500)

        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe false

        result = { "fetched_2" }
        scope.launch {
            state.mutate(data = {
                delay(1000)
                "mutated"
            }) {
                populateCache = false
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_2"
        state.error shouldBe null
        state.isValidating shouldBe false
    }

    @Test
    public fun mutationWithNoRollbackOnError() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched_1" }
        lateinit var scope: CoroutineScope
        lateinit var state: SWRState<String, String>
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            }) {
                dedupingInterval = 0.seconds
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1500)

        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe false

        result = { "fetched_2" }
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                state.mutate(data = {
                    delay(1000)
                    throw IllegalArgumentException()
                }) {
                    optimisticData = "optimisticData"
                    rollbackOnError = false
                }
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "optimisticData"
        state.error shouldBe null
        state.isValidating shouldBe false

        mutationError shouldBe null

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "optimisticData"
        state.error shouldBe null
        state.isValidating shouldBe false

        mutationError.shouldBeInstanceOf<IllegalArgumentException>()

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "optimisticData"
        state.error shouldBe null
        state.isValidating shouldBe false

        mutationError.shouldBeInstanceOf<IllegalArgumentException>()

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "optimisticData"
        state.error shouldBe null
        state.isValidating shouldBe false

        mutationError.shouldBeInstanceOf<IllegalArgumentException>()
    }
}
