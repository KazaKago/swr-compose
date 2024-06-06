package com.kazakago.swr.compose.config

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.NetworkRule
import com.kazakago.swr.compose.useSWR
import com.kazakago.swr.compose.useSWRConfig
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class GlobalConfigutationsTest {

    @get:Rule
    val networkRule = NetworkRule()

    @get:Rule
    val composeTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    fun defaultOptions() {
        val key = Random.nextInt().toString()
        val globalFetcher: suspend (Any) -> Any = { "${it}_fetched" }
        var data: String? = null
        composeTestRule.setContent {
            val (_, config) = useSWRConfig<Any, Any>()
            assertEquals(null, config.fetcher)
            assertEquals(2.seconds, config.dedupingInterval)
            SWRConfig(options = {
                fetcher = globalFetcher
                dedupingInterval = 10.seconds
            }) {
                val state = useSWR<String, String>(key = key) {
                    @Suppress("UNCHECKED_CAST")
                    assertEquals(globalFetcher, fetcher as suspend (Any) -> Any)
                    assertEquals(10.seconds, dedupingInterval)
                }
                data = state.data
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1)
        assertEquals("${key}_fetched", data)
    }

    @Test
    fun nestedDefaultOptions() {
        val globalFetcher1: suspend (Any) -> Any = { "${it}_fetched_1" }
        val globalFetcher2: suspend (Any) -> Any = { "${it}_fetched_2" }
        val key1 = "${object {}.javaClass.enclosingMethod?.name}_1"
        val key2 = "${object {}.javaClass.enclosingMethod?.name}_2"
        val key3 = "${object {}.javaClass.enclosingMethod?.name}_3"
        var data1: String? = null
        var data2: String? = null
        var data3: String? = null
        composeTestRule.setContent {
            val (_, config) = useSWRConfig<Any, Any>()
            assertEquals(null, config.fetcher)
            assertEquals(2.seconds, config.dedupingInterval)
            SWRConfig(options = {
                fetcher = globalFetcher1
                dedupingInterval = 10.seconds
            }) {
                val state1 = useSWR<String, String>(key = key1) {
                    @Suppress("UNCHECKED_CAST")
                    assertEquals(globalFetcher1, fetcher as suspend (Any) -> Any)
                    assertEquals(10.seconds, dedupingInterval)
                }
                data1 = state1.data

                SWRConfig(options = {
                    fetcher = globalFetcher2
                    dedupingInterval = 20.seconds
                }) {
                    val state2 = useSWR<String, String>(key = key2) {
                        @Suppress("UNCHECKED_CAST")
                        assertEquals(globalFetcher2, fetcher as suspend (Any) -> Any)
                        assertEquals(20.seconds, dedupingInterval)
                    }
                    data2 = state2.data
                }

                val state3 = useSWR<String, String>(key = key3) {
                    @Suppress("UNCHECKED_CAST")
                    assertEquals(globalFetcher1, fetcher as suspend (Any) -> Any)
                    assertEquals(10.seconds, dedupingInterval)
                }
                data3 = state3.data
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1)
        assertEquals("${key1}_fetched_1", data1)
        assertEquals("${key2}_fetched_2", data2)
        assertEquals("${key3}_fetched_1", data3)
    }
}
