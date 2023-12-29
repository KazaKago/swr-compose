package com.kazakago.swr.compose.config

public data class SWRTriggerConfig<KEY, DATA>(
    var optimisticData: DATA? = null,
    var revalidate: Boolean = true,
    var populateCache: Boolean = false,
    var rollbackOnError: Boolean = true,
    var throwOnError: Boolean = true,
    var onSuccess: ((data: DATA, key: KEY, config: SWRTriggerConfig<KEY, DATA>) -> Unit)? = null,
    var onError: ((error: Throwable, key: KEY, config: SWRTriggerConfig<KEY, DATA>) -> Unit)? = null,
)
