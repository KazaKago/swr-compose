package com.kazakago.swr.compose.mutate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

public val LocalSWRMutateConfig: ProvidableCompositionLocal<SWRMutateGlobalConfig> = compositionLocalOf {
    SWRMutateGlobalConfig()
}

@Composable
public fun SWRMutateConfig(
    options: SWRMutateGlobalConfig.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val config = SWRMutateGlobalConfig()
    config.options()
    CompositionLocalProvider(LocalSWRMutateConfig provides config) {
        content()
    }
}

public data class SWRMutateGlobalConfig(
    var revalidate: Boolean = true,
    var populateCache: Boolean = true,
    var rollbackOnError: Boolean = true,
    var throwOnError: Boolean = true,
)

public data class SWRMutateConfig<DATA>(
    var optimisticData: DATA? = null,
    var revalidate: Boolean,
    var populateCache: Boolean,
    var rollbackOnError: Boolean,
    var throwOnError: Boolean,
) {

    internal companion object {
        internal fun <DATA> from(globalConfig: SWRMutateGlobalConfig): SWRMutateConfig<DATA> {
            val config = globalConfig.copy()
            return SWRMutateConfig(
                revalidate = config.revalidate,
                populateCache = config.populateCache,
                rollbackOnError = config.rollbackOnError,
                throwOnError = config.throwOnError,
            )
        }
    }
}
