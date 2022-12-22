package com.kazakago.swr.compose.config

import com.kazakago.swr.compose.validate.SWRValidate
import com.kazakago.swr.compose.validate.SWRValidateOptions
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration

@Suppress("UNCHECKED_CAST")
public data class SWRInfiniteConfig<KEY, DATA>(
    override var fetcher: (suspend (key: KEY) -> DATA)?,
    override var revalidateIfStale: Boolean,
    override var revalidateOnMount: Boolean?,
    override var revalidateOnFocus: Boolean,
    override var revalidateOnReconnect: Boolean,
    override var refreshInterval: Duration,
    override var refreshWhenHidden: Boolean,
    override var refreshWhenOffline: Boolean,
    override var shouldRetryOnError: Boolean,
    override var dedupingInterval: Duration,
    override var focusThrottleInterval: Duration,
    override var loadingTimeout: Duration,
    override var errorRetryInterval: Duration,
    override var errorRetryCount: Int?,
    override var fallback: Map<KEY, DATA>,
    override var fallbackData: DATA? = null,
    override var keepPreviousData: Boolean,
    override var onLoadingSlow: ((key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
    override var onSuccess: ((data: DATA, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
    override var onError: ((error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
    override var onErrorRetry: suspend (error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>, revalidate: SWRValidate<KEY>, options: SWRValidateOptions) -> Unit,
    override var isPaused: () -> Boolean,
    public var initialSize: Int = 1,
    public var revalidateAll: Boolean = false,
    public var revalidateFirstPage: Boolean = true,
    public var persistSize: Boolean = false,
    override var scope: CoroutineScope?,
) : SWRConfig<KEY, DATA> {

    internal companion object {
        internal fun <KEY, DATA> from(globalConfig: SWRGlobalConfig): SWRInfiniteConfig<KEY, DATA> {
            val config = globalConfig.copy()
            return SWRInfiniteConfig(
                fetcher = config.fetcher as (suspend (key: KEY) -> DATA)?,
                revalidateIfStale = config.revalidateIfStale,
                revalidateOnMount = config.revalidateOnMount,
                revalidateOnFocus = config.revalidateOnFocus,
                revalidateOnReconnect = config.revalidateOnReconnect,
                refreshInterval = config.refreshInterval,
                refreshWhenHidden = config.refreshWhenHidden,
                refreshWhenOffline = config.refreshWhenOffline,
                shouldRetryOnError = config.shouldRetryOnError,
                dedupingInterval = config.dedupingInterval,
                focusThrottleInterval = config.focusThrottleInterval,
                loadingTimeout = config.loadingTimeout,
                errorRetryInterval = config.errorRetryInterval,
                errorRetryCount = config.errorRetryCount,
                fallback = config.fallback as Map<KEY, DATA>,
                keepPreviousData = config.keepPreviousData,
                onLoadingSlow = config.onLoadingSlow as ((key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
                onSuccess = config.onSuccess as ((data: DATA, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
                onError = config.onError as ((error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
                onErrorRetry = config.onErrorRetry as suspend (error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>, revalidate: SWRValidate<KEY>, options: SWRValidateOptions) -> Unit,
                isPaused = config.isPaused,
                scope = config.scope,
            )
        }
    }
}
