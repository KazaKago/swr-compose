package com.kazakago.swr.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.mutate.SWRMutate
import com.kazakago.swr.compose.mutate.SWRMutateImpl
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.state.SWRStateImpl
import com.kazakago.swr.compose.validate.SWRValidate
import com.kazakago.swr.compose.validate.SWRValidateImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun <KEY, DATA> useSWRInternal(
    key: KEY?,
    fetcher: (suspend (key: KEY) -> DATA)?,
    scope: CoroutineScope?,
    config: SWRConfig<KEY, DATA>,
): SWRState<KEY, DATA> {
    val cache = LocalSWRCache.current
    val systemCache = LocalSWRSystemCache.current
    val currentScope = scope ?: config.scope ?: rememberCoroutineScope()
    if (key != null && fetcher != null) systemCache.setFetcher(key, fetcher)
    val validate: SWRValidate<KEY> = remember(config, cache, systemCache) {
        SWRValidateImpl(config, cache, systemCache, fetcher)
    }
    val mutate: SWRMutate<KEY, DATA> = remember(key, cache, systemCache, validate) {
        SWRMutateImpl(key, cache, systemCache, validate)
    }

    val loadedDataHolder: DataHolder<DATA> = remember { DataHolder() }
    val loadedData: DATA? by remember(key) { cache.state(key) }
    val fallbackData: DATA? = remember(key) { config.fallbackData ?: config.fallback[key] ?: (if (config.keepPreviousData) loadedDataHolder.data else null) }
    val error: MutableState<Throwable?> = remember(key) { systemCache.errorState(key) }
    val isValidating: MutableState<Boolean> = remember(key) { systemCache.isValidatingState(key) }
    loadedDataHolder.data = loadedData

    RevalidateOnMount(key, config, isValidating) { currentScope.launch { validate(key) } }
    RevalidateOnFocus(key, config) { currentScope.launch { validate(key) } }
    RevalidateOnReconnect(key, config) { currentScope.launch { validate(key) } }
    RefreshInterval(key, config) { currentScope.launch { validate(key) } }

    return remember(key, loadedData, fallbackData, error.value, isValidating.value, mutate) {
        val data = loadedData ?: fallbackData
        val isLoading: Boolean = (loadedData == null) && (error.value == null) && isValidating.value
        SWRStateImpl(data = data, error = error.value, isLoading = isLoading, isValidating = isValidating.value, mutate = mutate)
    }
}
