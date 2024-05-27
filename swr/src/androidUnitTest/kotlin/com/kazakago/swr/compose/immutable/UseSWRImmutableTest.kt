package com.kazakago.swr.compose.immutable

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWRImmutable
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class UseSWRImmutableTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun immutable() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            LocalSWRCache.current.state<String, String>(key = key).let {
                if (it.value == null) it.value = "cached"
            }
            stateList += useSWRImmutable(key = key, fetcher = {
                delay(100)
                "fetched"
            })
        }

        composeTestRule.mainClock.advanceTimeBy(5000)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeTestRule.mainClock.advanceTimeBy(5000)

        assertEquals(listOf("cached"), stateList.map { it.data })
        assertEquals(listOf(null), stateList.map { it.error })
        assertEquals(listOf(false), stateList.map { it.isLoading })
        assertEquals(listOf(false), stateList.map { it.isValidating })
    }
}
