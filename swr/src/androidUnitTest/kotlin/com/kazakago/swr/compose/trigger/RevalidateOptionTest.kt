package com.kazakago.swr.compose.trigger

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRMutationState
import com.kazakago.swr.compose.useSWRMutation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class RevalidateOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun trigger_withRevalidate() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        lateinit var cache: MutableState<String?>
        composeTestRule.setContent {
            SWRConfig(options = {
                fetcher = { "fetcher" }
            }) {
                scope = rememberCoroutineScope()
                cache = LocalSWRCache.current.state(key)
                SWRGlobalScope = rememberCoroutineScope()
                stateList += useSWRMutation(key = key, fetcher = { _, arg ->
                    delay(100)
                    arg
                }) {
                    revalidate = true
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null), stateList.map { it.data })
        assertEquals(listOf(null), stateList.map { it.error })
        assertEquals(listOf(false), stateList.map { it.isMutating })
        var triggerResult: String? = null
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().trigger("trigger")
            }.onSuccess {
                triggerResult = it
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "trigger"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isMutating })
        assertEquals("trigger", triggerResult)
        assertEquals(null, mutationError)
        assertEquals("fetcher", cache.value)
    }

    @Test
    fun trigger_noRevalidate() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRMutationState<String, String, String>>()
        lateinit var scope: CoroutineScope
        lateinit var cache: MutableState<String?>
        composeTestRule.setContent {
            SWRConfig(options = {
                fetcher = { "fetcher" }
            }) {
                scope = rememberCoroutineScope()
                cache = LocalSWRCache.current.state(key)
                SWRGlobalScope = rememberCoroutineScope()
                stateList += useSWRMutation(key = key, fetcher = { _, arg ->
                    delay(100)
                    arg
                }) {
                    revalidate = false
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null), stateList.map { it.data })
        assertEquals(listOf(null), stateList.map { it.error })
        assertEquals(listOf(false), stateList.map { it.isMutating })
        var triggerResult: String? = null
        var mutationError: Throwable? = null
        scope.launch {
            runCatching {
                stateList.last().trigger("trigger")
            }.onSuccess {
                triggerResult = it
            }.onFailure {
                mutationError = it
            }
        }

        composeTestRule.mainClock.advanceTimeBy(2500)
        assertEquals(listOf(null, null, "trigger"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isMutating })
        assertEquals("trigger", triggerResult)
        assertEquals(null, mutationError)
        assertEquals(null, cache.value)
    }
}
