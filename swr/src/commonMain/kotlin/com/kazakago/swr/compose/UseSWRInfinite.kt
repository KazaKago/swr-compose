package com.kazakago.swr.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kazakago.swr.compose.config.LocalSWRConfig
import com.kazakago.swr.compose.config.SWRInfiniteConfig
import com.kazakago.swr.compose.config.SWRMutateConfig
import com.kazakago.swr.compose.internal.useSWRInternal
import com.kazakago.swr.compose.mutate.SWRMutate
import com.kazakago.swr.compose.state.SWRInfiniteState
import com.kazakago.swr.compose.state.SWRInfiniteStateImpl
import com.kazakago.swr.compose.state.SWRState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
public fun <KEY, DATA> useSWRInfinite(
    getKey: (pageIndex: Int, previousPageData: DATA?) -> KEY?,
    fetcher: (suspend (key: KEY) -> DATA)? = null,
    scope: CoroutineScope? = null,
    options: SWRInfiniteConfig<KEY, DATA>.() -> Unit = {},
): SWRInfiniteState<KEY, DATA> {
    val globalConfig = LocalSWRConfig.current
    val config = SWRInfiniteConfig.from<KEY, DATA>(globalConfig).apply { options() }
    val currentScope = scope ?: config.scope ?: rememberCoroutineScope()
    val pageIndexRememberKey: Any? = if (config.persistSize) Unit else getKey(1, null)
    var pageIndex by rememberSaveable(pageIndexRememberKey) { mutableIntStateOf(config.initialSize) }
    val pageKeyList: MutableList<KEY?> = mutableListOf()
    val pageStateList: MutableList<SWRState<KEY, DATA>> = mutableListOf()

    config.apply {
        revalidateIfStale = config.revalidateFirstPage || config.revalidateAll
        revalidateOnFocus = config.revalidateFirstPage || config.revalidateAll
        revalidateOnReconnect = config.revalidateFirstPage || config.revalidateAll
    }
    (0 until pageIndex).forEach { index ->
        pageKeyList += getKey(index + 1, pageStateList.getOrNull(index - 1)?.data)
        pageStateList += useSWRInternal(pageKeyList[index], fetcher, currentScope, config)
        config.apply {
            revalidateIfStale = config.revalidateAll
            revalidateOnFocus = config.revalidateAll
            revalidateOnReconnect = config.revalidateAll
        }
    }
    LaunchedEffect(pageIndex) {
        pageStateList.forEachIndexed { index, swrState ->
            if ((index == 0 && config.revalidateFirstPage) || config.revalidateAll) {
                currentScope.launch { swrState.mutate() }
            }
        }
    }
    val isDataAvailable = pageStateList.any { it.data != null }
    return SWRInfiniteStateImpl(
        data = if (isDataAvailable) pageStateList.map { it.data } else null,
        error = pageStateList.firstNotNullOfOrNull { it.error },
        isLoading = pageStateList.all { it.isLoading },
        isValidating = pageStateList.any { it.isValidating },
        mutate = object : SWRMutate<KEY, List<DATA>> {
            override suspend fun invoke(key: KEY?, data: (suspend () -> List<DATA>)?, options: SWRMutateConfig<List<DATA>>.() -> Unit) {
                coroutineScope {
                    val listMutateConfig = SWRMutateConfig<List<DATA>>().apply { options() }
                    val dataList = data?.invoke()
                    pageStateList.mapIndexed { index, swrState ->
                        async {
                            val dataBlock = dataList?.getOrNull(index)?.let { suspend { it } }
                            swrState.mutate(key, dataBlock) {
                                optimisticData = listMutateConfig.optimisticData?.getOrNull(index)
                                revalidate = listMutateConfig.revalidate
                                populateCache = listMutateConfig.populateCache
                                rollbackOnError = listMutateConfig.rollbackOnError
                            }
                        }
                    }.awaitAll()
                }
            }
        },
        size = pageIndex,
        setSize = { pageIndex = it },
    )
}
