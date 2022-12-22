package com.kazakago.swr.compose.immutable

import android.content.Context
import android.net.ConnectivityManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.useSWRImmutable
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowNetwork

@RunWith(AndroidJUnit4::class)
public class UseSWRImmutableTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun immutable() {
        val key = object {}.javaClass.enclosingMethod?.name
        val stateList = mutableListOf<SWRState<String, String>>()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            LocalSWRCache.current.state<String, String>(key = key!!).let {
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
        val connectivityManager = Shadows.shadowOf(composeTestRule.activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        connectivityManager.networkCallbacks.forEach { callback ->
            callback.onAvailable(ShadowNetwork.newInstance(1))
        }

        stateList.map { it.data } shouldBe listOf("cached")
        stateList.map { it.error } shouldBe listOf(null)
        stateList.map { it.isLoading } shouldBe listOf(false)
        stateList.map { it.isValidating } shouldBe listOf(false)
    }
}
