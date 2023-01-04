package com.kazakago.swr.compose.trigger

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import com.kazakago.swr.compose.cache.SWRCache
import com.kazakago.swr.compose.cache.SWRSystemCache
import com.kazakago.swr.compose.mutate.SWRMutateConfig
import com.kazakago.swr.compose.mutate.SWRMutateGlobalConfig

public interface SWRTrigger<KEY, DATA, ARG> {
    public suspend operator fun invoke(
        arg: ARG,
        options: SWRMutateConfig<DATA>.() -> Unit = {},
    )

    public companion object {
        public fun <KEY, DATA, ARG> empty(): SWRTrigger<KEY, DATA, ARG> = object : SWRTrigger<KEY, DATA, ARG> {
            override suspend fun invoke(arg: ARG, options: SWRMutateConfig<DATA>.() -> Unit) {}
        }
    }
}

@Immutable
internal data class SWRTriggerImpl<KEY, DATA, ARG>(
    private val key: KEY?,
    private val config: SWRMutateConfig<DATA>,
    private val cache: SWRCache,
    private val error: MutableState<Throwable?>,
    private val isMutating: MutableState<Boolean>,
    private val fetcher: (suspend (key: KEY, arg: ARG) -> DATA),
) : SWRTrigger<KEY, DATA, ARG> {

    override suspend fun invoke(
        arg: ARG,
        options: SWRMutateConfig<DATA>.() -> Unit,
    ) {
        val currentKey = key ?: return
        val config = config.apply { options() }
        val oldData: DATA? = cache.state<KEY, DATA>(currentKey).value
        if (config.optimisticData != null) {
            cache.state<KEY, DATA>(currentKey).value = config.optimisticData
            error.value = null
        }
        isMutating.value = true
        runCatching {
            fetcher(currentKey, arg)
        }.onSuccess { newData ->
            if (config.populateCache && newData != null) {
                cache.state<KEY, DATA>(currentKey).value = newData
                error.value = null
            }
        }.onFailure { throwable ->
            if (config.rollbackOnError) {
                cache.state<KEY, DATA>(currentKey).value = oldData
            }
            if (config.throwOnError) {
                throw throwable
            }
        }
        isMutating.value = false
    }
}
