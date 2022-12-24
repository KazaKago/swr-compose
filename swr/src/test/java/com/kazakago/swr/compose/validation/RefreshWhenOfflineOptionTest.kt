package com.kazakago.swr.compose.validation

import android.content.Context
import android.net.ConnectivityManager
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
import org.robolectric.Shadows.shadowOf
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
public class RefreshWhenOfflineOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun noRefreshWhenOffline() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.mainClock.autoAdvance = false
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
        stateList.map { it.data } shouldBe listOf(null, null, "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false)
    }

    @Test
    public fun withRefreshWhenOffline() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.mainClock.autoAdvance = false
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
        stateList.map { it.data } shouldBe listOf(null, null, "fetched", "fetched", "fetched", "fetched", "fetched", "fetched", "fetched")
        stateList.map { it.error } shouldBe listOf(null, null, null, null, null, null, null, null, null)
        stateList.map { it.isLoading } shouldBe listOf(false, true, false, false, false, false, false, false, false)
        stateList.map { it.isValidating } shouldBe listOf(false, true, false, true, false, true, false, true, false)
    }
}
