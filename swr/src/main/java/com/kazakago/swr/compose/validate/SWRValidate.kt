package com.kazakago.swr.compose.validate

import androidx.compose.runtime.Immutable
import com.kazakago.swr.compose.cache.SWRCache
import com.kazakago.swr.compose.cache.SWRSystemCache
import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.internal.SWRGlobalScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public interface SWRValidate<KEY> {
    public suspend operator fun invoke(
        key: KEY?,
        options: SWRValidateOptions? = null,
    )
}

@Immutable
internal data class SWRValidateImpl<KEY, DATA>(
    private val config: SWRConfig<KEY, DATA>,
    private val cache: SWRCache,
    private val systemCache: SWRSystemCache,
    private val _fetcher: (suspend (key: KEY) -> DATA)?,
) : SWRValidate<KEY> {

    override suspend operator fun invoke(
        key: KEY?,
        options: SWRValidateOptions?,
    ) {
        val currentKey = key ?: return
        val fetcher = _fetcher ?: systemCache.getFetcher(key) ?: config.fetcher ?: throw NotImplementedError("Fetcher cannot be null")
        val error = systemCache.errorState(currentKey)
        val isValidating = systemCache.isValidatingState(currentKey)
        val validatedTimerJob = systemCache.getValidatedTimerJob(currentKey)
        if (isValidating.value || config.isPaused()) return
        if (validatedTimerJob != null && validatedTimerJob.isActive) return

        isValidating.value = true
        val timeoutJob = CoroutineScope(currentCoroutineContext()).launch {
            delay(config.loadingTimeout)
            config.onLoadingSlow?.invoke(currentKey, config)
        }
        runCatching {
            fetcher(currentKey)
        }.onSuccess { newData ->
            timeoutJob.cancel()
            cache.state<KEY, DATA>(currentKey).value = newData
            val newValidatedTimerJob = SWRGlobalScope.launch { delay(config.dedupingInterval) }
            systemCache.setValidatedTimerJob(currentKey, newValidatedTimerJob)
            error.value = null
            config.onSuccess?.invoke(newData, currentKey, config)
        }.onFailure { throwable ->
            timeoutJob.cancel()
            val revalidateOptions = getValidateOptions(currentKey, options)
            error.value = throwable
            config.onError?.invoke(throwable, currentKey, config)
            if (config.shouldRetryOnError) {
                val job = CoroutineScope(currentCoroutineContext()).launch { config.onErrorRetry(throwable, currentKey, config, this@SWRValidateImpl, revalidateOptions) }
                systemCache.setRetryingJobSet(currentKey, systemCache.getRetryingJobSet(currentKey) + job)
            }
        }
        isValidating.value = false
    }

    private fun getValidateOptions(key: KEY, options: SWRValidateOptions?): SWRValidateOptions {
        return SWRValidateOptions(
            retryCount = (options?.retryCount ?: 0) + 1,
            dedupe = systemCache.getRetryingJobSet(key).any { it.children.none() && it.isActive },
        )
    }
}
