package com.kazakago.swr.compose.config

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kazakago.swr.compose.useSWR
import com.kazakago.swr.compose.useSWRConfig
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
public class GlobalConfigutationsTest {

    @get:Rule
    public val composeTestRule: ComposeContentTestRule = createComposeRule().apply {
        mainClock.autoAdvance = false
    }

    @Test
    public fun defaultOptions() {
        val key = object {}.javaClass.enclosingMethod?.name
        val globalFetcher: suspend (Any) -> Any = { "${it}_fetched" }
        var data: String? = null
        composeTestRule.setContent {
            val (_, config) = useSWRConfig<Any, Any>()
            config.fetcher shouldBe null
            config.dedupingInterval shouldBe 2.seconds
            SWRConfig(options = {
                fetcher = globalFetcher
                dedupingInterval = 10.seconds
            }) {
                val state = useSWR<String, String>(key = key) {
                    fetcher shouldBe globalFetcher
                    dedupingInterval shouldBe 10.seconds
                }
                data = state.data
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1)
        data shouldBe "${key}_fetched"
    }

    @Test
    public fun nestedDefaultOptions() {
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
            config.fetcher shouldBe null
            config.dedupingInterval shouldBe 2.seconds
            SWRConfig(options = {
                fetcher = globalFetcher1
                dedupingInterval = 10.seconds
            }) {
                val state1 = useSWR<String, String>(key = key1) {
                    fetcher shouldBe globalFetcher1
                    dedupingInterval shouldBe 10.seconds
                }
                data1 = state1.data

                SWRConfig(options = {
                    fetcher = globalFetcher2
                    dedupingInterval = 20.seconds
                }) {
                    val state2 = useSWR<String, String>(key = key2) {
                        fetcher shouldBe globalFetcher2
                        dedupingInterval shouldBe 20.seconds
                    }
                    data2 = state2.data
                }

                val state3 = useSWR<String, String>(key = key3) {
                    fetcher shouldBe globalFetcher1
                    dedupingInterval shouldBe 10.seconds
                }
                data3 = state3.data
            }
        }

        composeTestRule.mainClock.advanceTimeBy(1)
        data1 shouldBe "${key1}_fetched_1"
        data2 shouldBe "${key2}_fetched_2"
        data3 shouldBe "${key3}_fetched_1"
    }
}
