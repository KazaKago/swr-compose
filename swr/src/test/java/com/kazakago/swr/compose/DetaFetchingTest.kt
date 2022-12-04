package com.kazakago.swr.compose

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.state.SWRState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class DetaFetchingTest {

    @get:Rule
    public val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> = createAndroidComposeRule()

    @Test
    public fun validate() {
        val key = object {}.javaClass.enclosingMethod?.name
        lateinit var state: SWRState<String, String>
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            state = useSWR(key = key, fetcher = {
                delay(1000)
                "hoge"
            })
        }

        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe false

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe null
        state.error shouldBe null
        state.isValidating shouldBe true

        composeTestRule.mainClock.advanceTimeBy(500)
        state.data shouldBe "hoge"
        state.error shouldBe null
        state.isValidating shouldBe false
    }

    @Test
    public fun validateFailed() {
        val key = object {}.javaClass.enclosingMethod?.name
        lateinit var state: SWRState<String, String>
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            state = useSWR(key = key, fetcher = {
                delay(1000)
                throw NullPointerException()
            })
        }

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
    }
}
