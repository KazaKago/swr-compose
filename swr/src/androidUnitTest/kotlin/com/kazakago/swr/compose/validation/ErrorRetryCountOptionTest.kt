package com.kazakago.swr.compose.validation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.DummyException1
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.useSWR
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ErrorRetryCountOptionTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun errorRetryCountNull() {
        val key = Random.nextInt().toString()
        val errorRetryCountList = mutableListOf<Int?>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                errorRetryCount = null
                onErrorRetry = { _, _, config, _, _ ->
                    errorRetryCountList += config.errorRetryCount
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf<Int?>(null), errorRetryCountList)
    }

    @Test
    fun errorRetryCount3() {
        val key = Random.nextInt().toString()
        val errorRetryCountList = mutableListOf<Int?>()
        composeTestRule.setContent {
            SWRGlobalScope = rememberCoroutineScope()
            useSWR(key = key, fetcher = {
                delay(100)
                throw DummyException1
            }) {
                errorRetryCount = 3
                onErrorRetry = { _, _, config, _, _ ->
                    errorRetryCountList += config.errorRetryCount
                }
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        assertEquals(listOf<Int?>(3), errorRetryCountList)
    }
}
