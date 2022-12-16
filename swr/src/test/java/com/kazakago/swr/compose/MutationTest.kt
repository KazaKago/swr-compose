package com.kazakago.swr.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWREmptyCoroutineContext
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

@RunWith(AndroidJUnit4::class)
public class MutationTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun validate_mutation() {
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
            })
        }
        SWREmptyCoroutineContext = scope.coroutineContext

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(2000)
        result = { "fetched_2" }
        scope.launch { state.mutate() }

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
    public fun validate_mutationFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched" }
        lateinit var scope: CoroutineScope
        lateinit var state: SWRState<String, String>
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            })
        }
        SWREmptyCoroutineContext = scope.coroutineContext

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(2000)
        result = { throw IllegalArgumentException() }
        scope.launch { state.mutate() }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched"
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched"
        state.error.shouldBeInstanceOf<IllegalArgumentException>()
        state.isValidating shouldBe false
    }

    @Test
    public fun validate_mutationWithData() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched_1" }
        lateinit var state: SWRState<String, String>
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            })
        }
        SWREmptyCoroutineContext = scope.coroutineContext

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_1"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(2000)
        result = { "fetched_2" }
        scope.launch {
            state.mutate(data = {
                delay(1000)
                "mutated"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched_1"
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
    public fun validate_mutationWithDataFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { "fetched" }
        lateinit var state: SWRState<String, String>
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            })
        }
        SWREmptyCoroutineContext = scope.coroutineContext

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched"
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(2000)
        result = { throw IllegalArgumentException() }
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                state.mutate(data = {
                    delay(1000)
                    throw IllegalArgumentException()
                })
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched"
        state.error shouldBe null
        state.isValidating shouldBe false

        mutationError shouldBe null

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched"
        state.error shouldBe null
        state.isValidating shouldBe false

        mutationError.shouldBeInstanceOf<IllegalArgumentException>()
    }

    @Test
    public fun validateFailed_mutation() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { throw NullPointerException() }
        lateinit var state: SWRState<String, String>
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            })
        }
        SWREmptyCoroutineContext = scope.coroutineContext

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(2000)
        result = { "fetched" }
        scope.launch { state.mutate() }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "fetched"
        state.error shouldBe null
        state.isValidating shouldBe false
    }

    @Test
    public fun validateFailed_mutationFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { throw NullPointerException() }
        lateinit var state: SWRState<String, String>
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            })
        }
        SWREmptyCoroutineContext = scope.coroutineContext

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(2000)
        result = { throw IllegalArgumentException() }
        scope.launch { state.mutate() }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<IllegalArgumentException>()
        state.isValidating shouldBe false
    }

    @Test
    public fun validateFailed_mutationWithData() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { throw NullPointerException() }
        lateinit var state: SWRState<String, String>
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            })
        }
        SWREmptyCoroutineContext = scope.coroutineContext

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(2000)
        result = { "fetched" }
        scope.launch {
            runCatching {
                state.mutate(data = {
                    delay(1000)
                    "mutated"
                })
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
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
        state.data shouldBe "fetched"
        state.error shouldBe null
        state.isValidating shouldBe false
    }

    @Test
    public fun validateFailed_mutationWithDataFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        var result: () -> String = { throw NullPointerException() }
        lateinit var state: SWRState<String, String>
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            state = useSWR(key = key, scope = scope, fetcher = {
                delay(1000)
                result()
            })
        }
        SWREmptyCoroutineContext = scope.coroutineContext

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(2000)
        result = { throw IllegalArgumentException() }
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                state.mutate(data = {
                    delay(1000)
                    throw IllegalArgumentException()
                })
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
        state.isValidating shouldBe false

        mutationError shouldBe null

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error.shouldBeInstanceOf<NullPointerException>()
        state.isValidating shouldBe false

        mutationError.shouldBeInstanceOf<IllegalArgumentException>()
    }
}
