package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.config.LocalSWRConfig
import com.kazakago.swr.compose.config.SWRConfigImpl
import com.kazakago.swr.compose.preload.SWRPreload
import com.kazakago.swr.compose.preload.SWRPreloadImpl
import com.kazakago.swr.compose.validate.SWRValidate
import com.kazakago.swr.compose.validate.SWRValidateImpl

@Composable
public fun <KEY, DATA> useSWRPreload(
    key: () -> KEY?,
    fetcher: (suspend (key: KEY) -> DATA)? = null,
): SWRPreload {
    return useSWRPreload(runCatching(key).getOrNull(), fetcher)
}

@Composable
public fun <KEY, DATA> useSWRPreload(
    key: KEY?,
    fetcher: (suspend (key: KEY) -> DATA)? = null,
): SWRPreload {
    val globalConfig = LocalSWRConfig.current
    val config = SWRConfigImpl.from<KEY, DATA>(globalConfig)
    val cache = LocalSWRCache.current
    val systemCache = LocalSWRSystemCache.current
    if (key != null && fetcher != null) systemCache.setFetcher(key, fetcher)
    val validate: SWRValidate<KEY> = remember(config, cache, systemCache) {
        SWRValidateImpl(config, cache, systemCache, fetcher)
    }
    return SWRPreloadImpl(key, validate)
}
