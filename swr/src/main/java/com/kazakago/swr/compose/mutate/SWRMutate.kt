package com.kazakago.swr.compose.mutate

import androidx.compose.runtime.Immutable
import com.kazakago.swr.compose.cache.SWRCache
import com.kazakago.swr.compose.cache.SWRSystemCache
import com.kazakago.swr.compose.config.SWRMutateConfig
import com.kazakago.swr.compose.validate.SWRValidate

public interface SWRMutate<KEY, DATA> {
    public suspend operator fun invoke(
        key: KEY? = null,
        data: (suspend () -> DATA)? = null,
        options: SWRMutateConfig<DATA>.() -> Unit = {},
    )

    public companion object {
        public fun <KEY, DATA> empty(): SWRMutate<KEY, DATA> = object : SWRMutate<KEY, DATA> {
            override suspend fun invoke(key: KEY?, data: (suspend () -> DATA)?, options: SWRMutateConfig<DATA>.() -> Unit) {}
        }
    }
}

@Immutable
internal data class SWRMutateImpl<KEY, DATA>(
    private val key: KEY?,
    private val cache: SWRCache,
    private val systemCache: SWRSystemCache,
    private val validate: SWRValidate<KEY>,
) : SWRMutate<KEY, DATA> {

    override suspend operator fun invoke(
        key: KEY?,
        data: (suspend () -> DATA)?,
        options: SWRMutateConfig<DATA>.() -> Unit,
    ) {
        val currentKey = key ?: this.key ?: return
        val config = SWRMutateConfig<DATA>().apply { options() }
        val oldData: DATA? = cache.state<KEY, DATA>(currentKey).value
        if (config.optimisticData != null) {
            cache.state<KEY, DATA>(currentKey).value = config.optimisticData
            systemCache.errorState(currentKey).value = null
        }
        runCatching {
            if (data != null) data() else null
        }.onSuccess { newData ->
            if (config.populateCache && newData != null) {
                cache.state<KEY, DATA>(currentKey).value = newData
                systemCache.errorState(currentKey).value = null
            }
            if (config.revalidate) {
                validate(currentKey)
            }
        }.onFailure { throwable ->
            if (config.rollbackOnError) {
                cache.state<KEY, DATA>(currentKey).value = oldData
            }
            if (config.throwOnError) {
                throw throwable
            }
        }
    }
}
