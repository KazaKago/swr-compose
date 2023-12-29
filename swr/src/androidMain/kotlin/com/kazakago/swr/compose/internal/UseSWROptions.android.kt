package com.kazakago.swr.compose.internal

import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.kazakago.swr.compose.cache.LocalSWRSystemCache
import com.kazakago.swr.compose.config.SWRConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

@Composable
internal actual fun <KEY, DATA> RevalidateOnFocus(
    key: KEY?,
    config: SWRConfig<KEY, DATA>,
    validate: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val systemCache = LocalSWRSystemCache.current
    val revalidateOnFocus = config.revalidateOnFocus
    val focusThrottleInterval = config.focusThrottleInterval
    LaunchedEffect(key, lifecycleOwner, systemCache, revalidateOnFocus, focusThrottleInterval) {
        if (revalidateOnFocus) {
            var isFirstTime = true
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
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
        }
    }
}

@Composable
internal actual fun <KEY, DATA> RevalidateOnReconnect(
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
internal actual fun <KEY, DATA> RefreshInterval(
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
