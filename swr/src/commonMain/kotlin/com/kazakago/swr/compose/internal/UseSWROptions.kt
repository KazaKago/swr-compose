package com.kazakago.swr.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.config.SWRConfig
import kotlinx.coroutines.delay

@Composable
internal fun <KEY, DATA> RevalidateOnMount(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    isValidating: State<Boolean>,
    validate: () -> Unit,
) {
    val cache = LocalSWRCache.current
    val revalidateOnMount = config.revalidateOnMount
    val fallback = config.fallback
    val fallbackData = config.fallbackData
    val revalidateIfStale = config.revalidateIfStale
    LaunchedEffect(key, cache, revalidateOnMount, fallback, fallbackData, revalidateIfStale) {
        val fixedRevalidateOnMount = revalidateOnMount ?: (fallbackData == null && fallback[key] == null)
        if (fixedRevalidateOnMount) {
            var mutableRevalidateIfStale = revalidateIfStale
            while (true) {
                if (isValidating.value) {
                    delay(100)
                    mutableRevalidateIfStale = false
                    continue
                }
                val data = cache.state<KEY?, DATA>(key).value
                if (mutableRevalidateIfStale || data == null) {
                    validate()
                }
                break
            }
        }
    }
}

@Composable
internal expect fun <KEY, DATA> RevalidateOnFocus(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
)

@Composable
internal expect fun <KEY, DATA> RevalidateOnReconnect(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
)

@Composable
internal expect fun <KEY, DATA> RefreshInterval(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
)
