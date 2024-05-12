package com.kazakago.swr.compose.cache

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

public val LocalSWRCache: ProvidableCompositionLocal<SWRCache> = compositionLocalOf {
    SWRCacheImpl()
}

public interface SWRCache {
    public fun <KEY, DATA> state(key: KEY): MutableState<DATA?>
    public fun clear()
}

@Suppress("UNCHECKED_CAST")
internal class SWRCacheImpl : SWRCache {

    private val cacheMap: MutableMap<Any?, MutableState<Any?>> = mutableMapOf()

    override fun <KEY, DATA> state(key: KEY): MutableState<DATA?> {
        return (cacheMap as MutableMap<KEY, MutableState<DATA?>>).getOrPut(key) { mutableStateOf(null) }
    }

    override fun clear() {
        cacheMap.clear()
    }
}
