package com.kazakago.swr.compose.retry

import com.kazakago.swr.compose.config.SWRConfig
import com.kazakago.swr.compose.validate.SWRValidate
import com.kazakago.swr.compose.validate.SWRValidateOptions
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.random.Random

public val OnErrorRetryDefault: suspend (error: Throwable, key: Any, config: SWRConfig<*, *>, revalidate: SWRValidate<Any>, options: SWRValidateOptions) -> Unit = { _, key, config, revalidate, options ->
    if (!options.dedupe && config.errorRetryCount.let { it == null || options.retryCount <= it }) {
        val exponentialBackoff = floor((Random.nextDouble() + 0.5) * 1.shl(options.retryCount)).toLong() * config.errorRetryInterval.inWholeMilliseconds
        delay(exponentialBackoff)
        revalidate(key, options)
    }
}
