package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import com.kazakago.swr.compose.config.LocalSWRConfig
import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.config.SWRConfigImpl
import com.kazakago.swr.compose.internal.useSWRInternal
import com.kazakago.swr.compose.state.SWRState
import kotlinx.coroutines.CoroutineScope

@Composable
public fun <KEY, DATA> useSWRImmutable(
    key: () -> KEY?,
    fetcher: (suspend (key: KEY) -> DATA)? = null,
    scope: CoroutineScope? = null,
    options: SWRConfig<KEY, DATA>.() -> Unit = {},
): SWRState<KEY, DATA> {
    return useSWRImmutable(runCatching(key).getOrNull(), fetcher, scope, options)
}

@Composable
public fun <KEY, DATA> useSWRImmutable(
    key: KEY?,
    fetcher: (suspend (key: KEY) -> DATA)? = null,
    scope: CoroutineScope? = null,
    options: SWRConfig<KEY, DATA>.() -> Unit = {},
): SWRState<KEY, DATA> {
    val globalConfig = LocalSWRConfig.current
    val config = SWRConfigImpl.from<KEY, DATA>(globalConfig).apply {
        options()
        revalidateIfStale = false
        revalidateOnFocus = false
        revalidateOnReconnect = false
    }
    return useSWRInternal(key, fetcher, scope, config)
}
