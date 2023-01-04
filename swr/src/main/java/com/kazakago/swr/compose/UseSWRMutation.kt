package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.mutate.LocalSWRMutateConfig
import com.kazakago.swr.compose.mutate.SWRMutateConfig
import com.kazakago.swr.compose.state.SWRMutationState
import com.kazakago.swr.compose.state.SWRMutationStateImpl
import com.kazakago.swr.compose.trigger.SWRReset
import com.kazakago.swr.compose.trigger.SWRResetImpl
import com.kazakago.swr.compose.trigger.SWRTrigger
import com.kazakago.swr.compose.trigger.SWRTriggerImpl

@Composable
public fun <KEY, DATA, ARG> useSWRMutation(
    key: () -> KEY?,
    fetcher: (suspend (key: KEY, arg: ARG) -> DATA),
    options: SWRMutateConfig<DATA>.() -> Unit = {},
): SWRMutationState<KEY, DATA, ARG> {
    return useSWRMutation(runCatching(key).getOrNull(), fetcher, options)
}

@Composable
public fun <KEY, DATA, ARG> useSWRMutation(
    key: KEY?,
    fetcher: (suspend (key: KEY, arg: ARG) -> DATA),
    options: SWRMutateConfig<DATA>.() -> Unit = {},
): SWRMutationState<KEY, DATA, ARG> {
    val cache = LocalSWRCache.current
    val globalConfig = LocalSWRMutateConfig.current
    val config = SWRMutateConfig.from<DATA>(globalConfig).apply { options() }
    val data: MutableState<DATA?> = remember(key) { mutableStateOf(null) }
    val error: MutableState<Throwable?> = remember(key) { mutableStateOf(null) }
    val isMutating: MutableState<Boolean> = remember(key) { mutableStateOf(false) }
    val trigger: SWRTrigger<KEY, DATA, ARG> = remember(key, config, cache) {
        SWRTriggerImpl(key, config, cache, data, error, isMutating, fetcher)
    }
    val reset: SWRReset<KEY, DATA> = remember(key) {
        SWRResetImpl(data, error, isMutating)
    }
    return remember(key, data.value, error.value, trigger, reset, isMutating.value) {
        SWRMutationStateImpl(data = data.value, error = error.value, trigger = trigger, reset = reset, isMutating = isMutating.value)
    }
}
