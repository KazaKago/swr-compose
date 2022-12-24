package com.kazakago.swr.compose.internal

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.mutate.LocalSWRMutateConfig
import com.kazakago.swr.compose.mutate.SWRMutate
import com.kazakago.swr.compose.mutate.SWRMutateImpl
import com.kazakago.swr.compose.state.SWRState
import com.kazakago.swr.compose.state.SWRStateImpl
import com.kazakago.swr.compose.validate.SWRValidate
import com.kazakago.swr.compose.validate.SWRValidateImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

@Composable
internal fun <KEY, DATA> useSWRInternal(
    key: KEY?,
    fetcher: (suspend (key: KEY) -> DATA)?,
    _scope: CoroutineScope?,
    config: SWRConfig<KEY, DATA>,
): SWRState<KEY, DATA> {
    val cache = LocalSWRCache.current
    val systemCache = LocalSWRSystemCache.current
    val mutateGlobalConfig = LocalSWRMutateConfig.current
    val scope = _scope ?: config.scope ?: rememberCoroutineScope()
    if (key != null && fetcher != null) systemCache.setFetcher(key, fetcher)
    val validate: SWRValidate<KEY> = remember(config, cache, systemCache) {
        SWRValidateImpl(config, cache, systemCache, fetcher)
    }
    val mutate: SWRMutate<KEY, DATA> = remember(key, mutateGlobalConfig, cache, systemCache, validate) {
        SWRMutateImpl(key, mutateGlobalConfig, cache, systemCache, validate)
    }

    val oldDataHolder: DataHolder<DATA> = remember { DataHolder() }
    val data: DATA? by remember(key) { cache.state(key) }
    val fallbackData: DATA? = remember(key) { config.fallbackData ?: config.fallback[key] ?: (if (config.keepPreviousData) oldDataHolder.data else null) }
    val error: MutableState<Throwable?> = remember(key) { systemCache.errorState(key) }
    val isValidating: MutableState<Boolean> = remember(key) { systemCache.isValidatingState(key) }
    oldDataHolder.data = data

    RevalidateOnMount(key, config, isValidating) { scope.launch { validate(key) } }
    RevalidateOnFocus(key, config) { scope.launch { validate(key) } }
    RevalidateOnReconnect(key, config) { scope.launch { validate(key) } }
    RefreshInterval(key, config) { scope.launch { validate(key) } }

    return remember(key, data, fallbackData, error.value, isValidating.value, mutate) {
        val mergedData = data ?: fallbackData
        val isLoading: Boolean = (data == null) && (error.value == null)
        SWRStateImpl(data = mergedData, error = error.value, isLoading = isLoading, isValidating = isValidating.value, mutate = mutate)
    }
}

@Composable
internal fun <KEY, DATA> RevalidateOnMount(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    isValidating: State<Boolean>,
    validate: () -> Unit,
) {
    val cache = LocalSWRCache.current
    val revalidateOnMount = config.revalidateOnMount
    val fallback = config.fallback
    val fallbackData = config.fallbackData
    val revalidateIfStale = config.revalidateIfStale
    LaunchedEffect(key, cache, revalidateOnMount, fallback, fallbackData, revalidateIfStale) {
        val fixedRevalidateOnMount = revalidateOnMount ?: (fallbackData == null && fallback[key] == null)
        if (fixedRevalidateOnMount) {
            var mutableRevalidateIfStale = revalidateIfStale
            while (true) {
                if (isValidating.value) {
                    delay(100)
                    mutableRevalidateIfStale = false
                    continue
                }
                val data = cache.state<KEY?, DATA>(key).value
                if (mutableRevalidateIfStale || data == null) {
                    validate()
                }
                break
            }
        }
    }
}

@Composable
internal fun <KEY, DATA> RevalidateOnFocus(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val systemCache = LocalSWRSystemCache.current
    val revalidateOnFocus = config.revalidateOnFocus
    val focusThrottleInterval = config.focusThrottleInterval
    DisposableEffect(key, lifecycleOwner, systemCache, revalidateOnFocus, focusThrottleInterval) {
        if (revalidateOnFocus) {
            var isFirstTime = true
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        val focusedTimerJob = systemCache.getFocusedTimerJob(key)
                        if (!isFirstTime) {
                            if (focusedTimerJob == null || !focusedTimerJob.isActive) {
                                val newFocusedTimerJob = SWRGlobalScope.launch { delay(focusThrottleInterval) }
                                systemCache.setFocusedTimerJob(key, newFocusedTimerJob)
                                validate()
                            }
                        }
                        isFirstTime = false
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        } else {
            onDispose {}
        }
    }
}

@Composable
internal fun <KEY, DATA> RevalidateOnReconnect(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val revalidateOnReconnect = config.revalidateOnReconnect
    DisposableEffect(key, lifecycleOwner, revalidateOnReconnect) {
        if (revalidateOnReconnect) {
            var shouldNotValidateAtNextOnAvailable = false
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (!shouldNotValidateAtNextOnAvailable) {
                        validate()
                    }
                    shouldNotValidateAtNextOnAvailable = false
                }
            }
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        shouldNotValidateAtNextOnAvailable = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.let {
                            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        } ?: false
                        connectivityManager.registerNetworkCallback(request, networkCallback)
                    }
                    Lifecycle.Event.ON_STOP -> {
                        connectivityManager.unregisterNetworkCallback(networkCallback)
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        } else {
            onDispose {}
        }
    }
}

@Composable
internal fun <KEY, DATA> RefreshInterval(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val refreshInterval = config.refreshInterval
    val refreshWhenHidden = config.refreshWhenHidden
    val refreshWhenOffline = config.refreshWhenOffline
    LaunchedEffect(key, refreshInterval, refreshWhenHidden, refreshWhenOffline) {
        if (refreshInterval > Duration.ZERO) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            while (true) {
                delay(refreshInterval)
                if (refreshWhenHidden || lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    if (refreshWhenOffline || connectivityManager.activeNetwork != null) {
                        validate()
                    }
                }
            }
        }
    }
}
