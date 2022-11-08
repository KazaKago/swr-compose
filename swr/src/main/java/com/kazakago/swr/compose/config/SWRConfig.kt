package com.kazakago.swr.compose.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.kazakago.swr.compose.retry.OnErrorRetryDefault
import com.kazakago.swr.compose.validate.SWRValidate
import com.kazakago.swr.compose.validate.SWRValidateOptions
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public val LocalSWRConfig: ProvidableCompositionLocal<SWRGlobalConfig> = compositionLocalOf {
    SWRGlobalConfig()
}

@Composable
public fun SWRConfig(
    options: SWRGlobalConfig.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val config = SWRGlobalConfig()
    config.options()
    CompositionLocalProvider(LocalSWRConfig provides config) {
        content()
    }
}

public data class SWRGlobalConfig(
    var fetcher: (suspend (key: Any) -> Any)? = null,
    var revalidateIfStale: Boolean = true,
    var revalidateOnMount: Boolean? = null,
    var revalidateOnFocus: Boolean = true,
    var revalidateOnReconnect: Boolean = true,
    var refreshInterval: Duration = Duration.ZERO,
    var refreshWhenHidden: Boolean = false,
    var refreshWhenOffline: Boolean = false,
    var shouldRetryOnError: Boolean = true,
    var dedupingInterval: Duration = 2.seconds,
    var focusThrottleInterval: Duration = 5.seconds,
    var loadingTimeout: Duration = 3.seconds,
    var errorRetryInterval: Duration = 5.seconds,
    var errorRetryCount: Int? = null,
    var fallback: Map<Any, Any> = emptyMap(),
    var onLoadingSlow: ((key: Any, config: SWRConfig<*, *>) -> Unit)? = null,
    var onSuccess: ((data: Any, key: Any, config: SWRConfig<*, *>) -> Unit)? = null,
    var onError: ((error: Throwable, key: Any, config: SWRConfig<*, *>) -> Unit)? = null,
    var onErrorRetry: suspend (error: Throwable, key: Any, config: SWRConfig<*, *>, revalidate: SWRValidate<Any>, options: SWRValidateOptions) -> Unit = OnErrorRetryDefault,
    var isPaused: () -> Boolean = { false },
    var scope: CoroutineScope? = null,
)

public interface SWRConfig<KEY, DATA> {
    public var fetcher: (suspend (key: KEY) -> DATA)?
    public var revalidateIfStale: Boolean
    public var revalidateOnMount: Boolean?
    public var revalidateOnFocus: Boolean
    public var revalidateOnReconnect: Boolean
    public var refreshInterval: Duration
    public var refreshWhenHidden: Boolean
    public var refreshWhenOffline: Boolean
    public var shouldRetryOnError: Boolean
    public var dedupingInterval: Duration
    public var focusThrottleInterval: Duration
    public var loadingTimeout: Duration
    public var errorRetryInterval: Duration
    public var errorRetryCount: Int?
    public var fallback: Map<KEY, DATA>
    public var fallbackData: DATA?
    public var onLoadingSlow: ((key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?
    public var onSuccess: ((data: DATA, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?
    public var onError: ((error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?
    public var onErrorRetry: suspend (error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>, revalidate: SWRValidate<KEY>, options: SWRValidateOptions) -> Unit
    public var isPaused: () -> Boolean
    public var scope: CoroutineScope?
}

@Suppress("UNCHECKED_CAST")
internal data class SWRConfigImpl<KEY, DATA>(
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
    override var onLoadingSlow: ((key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
    override var onSuccess: ((data: DATA, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
    override var onError: ((error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>) -> Unit)?,
    override var onErrorRetry: suspend (error: Throwable, key: KEY, config: SWRConfig<KEY, DATA>, revalidate: SWRValidate<KEY>, options: SWRValidateOptions) -> Unit,
    override var isPaused: () -> Boolean,
    override var scope: CoroutineScope?,
) : SWRConfig<KEY, DATA> {

    internal companion object {
        internal fun <KEY, DATA> from(globalConfig: SWRGlobalConfig): SWRConfig<KEY, DATA> {
            val config = globalConfig.copy()
            return SWRConfigImpl(
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
