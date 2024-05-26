package com.kazakago.swr.compose.validation

import android.content.Context
import android.net.ConnectivityManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
public class RefreshWhenOfflineOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun noRefreshWhenOffline() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                refreshInterval = 10.seconds
                refreshWhenOffline = false
            }
        }

        val connectivityManager = shadowOf(composeTestRule.activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        connectivityManager.setDefaultNetworkActive(false)

        composeTestRule.mainClock.advanceTimeBy(35000)
        assertEquals(listOf(null, null, "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false), stateList.map { it.isValidating })
    }

    @Test
    public fun withRefreshWhenOffline() {
        val key = Random.nextInt().toString()
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            stateList += useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                refreshInterval = 10.seconds
                refreshWhenOffline = true
            }
        }

        val connectivityManager = shadowOf(composeTestRule.activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        connectivityManager.setDefaultNetworkActive(false)

        composeTestRule.mainClock.advanceTimeBy(35000)
        assertEquals(listOf(null, null, "fetched", "fetched", "fetched", "fetched", "fetched", "fetched", "fetched"), stateList.map { it.data })
        assertEquals(listOf(null, null, null, null, null, null, null, null, null), stateList.map { it.error })
        assertEquals(listOf(false, true, false, false, false, false, false, false, false), stateList.map { it.isLoading })
        assertEquals(listOf(false, true, false, true, false, true, false, true, false), stateList.map { it.isValidating })
    }
}
