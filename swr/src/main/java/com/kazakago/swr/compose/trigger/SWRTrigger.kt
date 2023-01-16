package com.kazakago.swr.compose.trigger

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import com.kazakago.swr.compose.cache.SWRCache
import com.kazakago.swr.compose.config.SWRTriggerConfig
import com.kazakago.swr.compose.internal.SWRGlobalScope
import com.kazakago.swr.compose.validate.SWRValidate
import kotlinx.coroutines.launch

public interface SWRTrigger<KEY, DATA, ARG> {
    public suspend operator fun invoke(
        arg: ARG,
        options: SWRTriggerConfig<KEY, DATA>.() -> Unit = {},
    ): DATA?

    public companion object {
        public fun <KEY, DATA, ARG> empty(): SWRTrigger<KEY, DATA, ARG> = object : SWRTrigger<KEY, DATA, ARG> {
            override suspend fun invoke(arg: ARG, options: SWRTriggerConfig<KEY, DATA>.() -> Unit): DATA? = null
        }
    }
}

@Immutable
internal data class SWRTriggerImpl<KEY, DATA, ARG>(
    private val key: KEY?,
    private val config: SWRTriggerConfig<KEY, DATA>,
    private val cache: SWRCache,
    private val data: MutableState<DATA?>,
    private val error: MutableState<Throwable?>,
    private val isMutating: MutableState<Boolean>,
    private val fetcher: (suspend (key: KEY, arg: ARG) -> DATA),
    private val validate: SWRValidate<KEY>,
) : SWRTrigger<KEY, DATA, ARG> {

    override suspend fun invoke(
        arg: ARG,
        options: SWRTriggerConfig<KEY, DATA>.() -> Unit,
    ): DATA? {
        val currentKey = key ?: return null
        val config = config.apply { options() }
        val oldData: DATA? = data.value
        if (config.optimisticData != null) {
            data.value = config.optimisticData
            error.value = null
        }
        isMutating.value = true
        runCatching {
            fetcher(currentKey, arg)
        }.onSuccess { newData ->
            data.value = newData
            error.value = null
            isMutating.value = false
            config.onSuccess?.invoke(newData, currentKey, config)
            if (config.populateCache && newData != null) {
                cache.state<KEY, DATA>(currentKey).value = newData
            }
            if (config.revalidate) {
                SWRGlobalScope.launch { validate(currentKey) }
            }
        }.onFailure { throwable ->
            isMutating.value = false
            config.onError?.invoke(throwable, currentKey, config)
            if (config.rollbackOnError) {
                data.value = oldData
            }
            if (config.throwOnError) {
                error.value = throwable
                throw throwable
            }
        }
        return data.value
    }
}
