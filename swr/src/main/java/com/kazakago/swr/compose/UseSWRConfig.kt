package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.config.LocalSWRConfig
import com.kazakago.swr.compose.config.SWRConfigImpl
import com.kazakago.swr.compose.mutate.SWRMutate
import com.kazakago.swr.compose.mutate.SWRMutateImpl
import com.kazakago.swr.compose.state.SWRConfigState
import com.kazakago.swr.compose.state.SWRConfigStateImpl
import com.kazakago.swr.compose.validate.SWRValidate
import com.kazakago.swr.compose.validate.SWRValidateImpl

@Composable
public fun <KEY, DATA> useSWRConfig(): SWRConfigState<KEY, DATA> {
    val cache = LocalSWRCache.current
    val systemCache = LocalSWRSystemCache.current
    val globalConfig = LocalSWRConfig.current
    val config = SWRConfigImpl.from<KEY, DATA>(globalConfig)
    val validate: SWRValidate<KEY> = remember(config, cache, systemCache) {
        SWRValidateImpl(config, cache, systemCache, null)
    }
    val mutate: SWRMutate<KEY, DATA> = remember(cache, systemCache, validate) {
        SWRMutateImpl(null, cache, systemCache, validate)
    }
    return SWRConfigStateImpl(
        mutate = mutate,
        config = config,
        cache = cache,
    )
}
