package com.kazakago.swr.compose.validation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.useSWR
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class OnErrorOptionTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun withOnError() {
        val key = object {}.javaClass.enclosingMethod?.name
        val onErrorList = mutableListOf<Pair<Throwable, String>>()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                onSuccess = { _, _, _ ->
                    fail("Must not reach here")
                }
                onError = { error, key, _ ->
                    onErrorList += error to key
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(3000)
        onErrorList shouldBe listOf(DummyException1 to key)
    }
}
