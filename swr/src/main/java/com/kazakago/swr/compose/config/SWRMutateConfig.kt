package com.kazakago.swr.compose.config

public data class SWRMutateConfig<DATA>(
    var optimisticData: DATA? = null,
    var revalidate: Boolean = true,
    var populateCache: Boolean = true,
    var rollbackOnError: Boolean = true,
    var throwOnError: Boolean = true,
)
