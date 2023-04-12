package com.kazakago.swr.compose.internal

import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import com.kazakago.swr.compose.cache.LocalSWRCache
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.config.SWRConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

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
    LaunchedEffect(key, lifecycleOwner, revalidateOnReconnect) {
        if (revalidateOnReconnect) {
            val networkCallbackManager = NetworkCallbackManager.getInstance(context)
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkCallbackManager.onAvailable.collect {
                    validate()
                }
            }
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
