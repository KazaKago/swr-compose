package com.kazakago.swr.compose.validation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.useSWR
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class OnSuccessOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule<ComponentActivity>().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun withOnSuccess() {
        val key = object {}.javaClass.enclosingMethod?.name
        val onSuccessList = mutableListOf<Pair<String, String>>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                "fetched"
            }) {
                onSuccess = { data, key, _ ->
                    onSuccessList += data to key
                }
                onError = { _, _, _ ->
                    fail("Must not reach here")
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(3000)
        onSuccessList shouldBe listOf("fetched" to key)
    }
}
