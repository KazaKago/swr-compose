package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.config.LocalSWRConfig
import com.kazakago.swr.compose.config.SWRConfigImpl
import com.kazakago.swr.compose.config.SWRTriggerConfig
import com.kazakago.swr.compose.state.SWRMutationState
import com.kazakago.swr.compose.state.SWRMutationStateImpl
import com.kazakago.swr.compose.trigger.SWRReset
import com.kazakago.swr.compose.trigger.SWRResetImpl
import com.kazakago.swr.compose.trigger.SWRTrigger
import com.kazakago.swr.compose.trigger.SWRTriggerImpl
import com.kazakago.swr.compose.validate.SWRValidate
import com.kazakago.swr.compose.validate.SWRValidateImpl

@Composable
public fun <KEY, DATA, ARG> useSWRMutation(
    key: () -> KEY?,
    fetcher: (suspend (key: KEY, arg: ARG) -> DATA),
    options: SWRTriggerConfig<KEY, DATA>.() -> Unit = {},
): SWRMutationState<KEY, DATA, ARG> {
    return useSWRMutation(runCatching(key).getOrNull(), fetcher, options)
}

@Composable
public fun <KEY, DATA, ARG> useSWRMutation(
    key: KEY?,
    fetcher: (suspend (key: KEY, arg: ARG) -> DATA),
    options: SWRTriggerConfig<KEY, DATA>.() -> Unit = {},
): SWRMutationState<KEY, DATA, ARG> {
    val cache = LocalSWRCache.current
    val systemCache = LocalSWRSystemCache.current
    val globalConfig = LocalSWRConfig.current
    val config = SWRConfigImpl.from<KEY, DATA>(globalConfig)
    val mutateConfig = SWRTriggerConfig<KEY, DATA>().apply { options() }
    val data: MutableState<DATA?> = remember(key) { mutableStateOf(null) }
    val error: MutableState<Throwable?> = remember(key) { mutableStateOf(null) }
    val isMutating: MutableState<Boolean> = remember(key) { mutableStateOf(false) }
    val validate: SWRValidate<KEY> = remember(mutateConfig, cache, systemCache) {
        SWRValidateImpl(config, cache, systemCache, null)
    }
    val trigger: SWRTrigger<KEY, DATA, ARG> = remember(key, mutateConfig, cache, validate) {
        SWRTriggerImpl(key, mutateConfig, cache, data, error, isMutating, fetcher, validate)
    }
    val reset: SWRReset<KEY, DATA> = remember(key) {
        SWRResetImpl(data, error, isMutating)
    }
    return remember(key, data.value, error.value, trigger, reset, isMutating.value) {
        SWRMutationStateImpl(data = data.value, error = error.value, trigger = trigger, reset = reset, isMutating = isMutating.value)
    }
}
