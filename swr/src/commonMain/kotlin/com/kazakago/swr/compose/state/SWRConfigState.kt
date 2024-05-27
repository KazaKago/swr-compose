package com.kazakago.swr.compose.state

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import com.kazakago.swr.compose.cache.SWRCache
import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.config.SWRConfigImpl
import com.kazakago.swr.compose.config.SWRGlobalConfig
import com.kazakago.swr.compose.mutate.SWRMutate

public interface SWRConfigState<KEY, DATA> {
    public val mutate: SWRMutate<KEY, DATA>
    public val config: SWRConfig<KEY, DATA>
    public val cache: SWRCache

    public operator fun component1(): SWRMutate<KEY, DATA> = mutate
    public operator fun component2(): SWRConfig<KEY, DATA> = config
    public operator fun component3(): SWRCache = cache

    public companion object {
        public fun <KEY, DATA> empty(
            mutate: SWRMutate<KEY, DATA> = SWRMutate.empty(),
            config: SWRConfig<KEY, DATA> = SWRConfigImpl.from(SWRGlobalConfig()),
            cache: SWRCache = object : SWRCache {
                override fun <KEY, DATA> state(key: KEY) = mutableStateOf<DATA?>(null)
                override fun clear() {}
            }
        ): SWRConfigState<KEY, DATA> = SWRConfigStateImpl(mutate, config, cache)
    }

}

@Immutable
internal data class SWRConfigStateImpl<KEY, DATA>(
    override val mutate: SWRMutate<KEY, DATA>,
    override val config: SWRConfig<KEY, DATA>,
    override val cache: SWRCache,
) : SWRConfigState<KEY, DATA>
