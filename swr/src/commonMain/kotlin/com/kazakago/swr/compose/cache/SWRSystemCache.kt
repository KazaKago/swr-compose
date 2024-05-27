package com.kazakago.swr.compose.cache

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Job

public val LocalSWRSystemCache: ProvidableCompositionLocal<SWRSystemCache> = compositionLocalOf {
    SWRSystemCacheImpl()
}

public interface SWRSystemCache {
    public fun <KEY> isValidatingState(key: KEY): MutableState<Boolean>
    public fun <KEY> errorState(key: KEY): MutableState<Throwable?>
    public fun <KEY, DATA> getFetcher(key: KEY): (suspend (key: KEY) -> DATA)?
    public fun <KEY, DATA> setFetcher(key: KEY, fetcher: (suspend (key: KEY) -> DATA))
    public fun <KEY> getValidatedTimerJob(key: KEY): Job?
    public fun <KEY> setValidatedTimerJob(key: KEY, job: Job)
    public fun <KEY> getFocusedTimerJob(key: KEY): Job?
    public fun <KEY> setFocusedTimerJob(key: KEY, job: Job)
    public fun <KEY> getRetryingJobSet(key: KEY): Set<Job>
    public fun <KEY> setRetryingJobSet(key: KEY, jobs: Set<Job>)
    public fun clear()
}

@Suppress("UNCHECKED_CAST")
internal class SWRSystemCacheImpl : SWRSystemCache {

    private val isValidatingStateMap: MutableMap<Any, MutableState<Boolean>> = mutableMapOf()
    private val errorStateMap: MutableMap<Any, MutableState<Throwable?>> = mutableMapOf()
    private val fetcherMap: MutableMap<Any, (suspend (key: Any) -> Any)> = mutableMapOf()
    private val validatedTimeMap: MutableMap<Any, Job> = mutableMapOf()
    private val focusedTimeMap: MutableMap<Any, Job> = mutableMapOf()
    private val isRetryingJobSetMap: MutableMap<Any, Set<Job>> = mutableMapOf()

    override fun <KEY> isValidatingState(key: KEY): MutableState<Boolean> {
        return (isValidatingStateMap as MutableMap<KEY, MutableState<Boolean>>).getOrPut(key) {
            mutableStateOf(false)
        }
    }

    override fun <KEY> errorState(key: KEY): MutableState<Throwable?> {
        return (errorStateMap as MutableMap<KEY, MutableState<Throwable?>>).getOrPut(key) {
            mutableStateOf(null)
        }
    }

    override fun <KEY, DATA> getFetcher(key: KEY): (suspend (key: KEY) -> DATA)? {
        return (fetcherMap as MutableMap<KEY, (suspend (key: KEY) -> DATA)>)[key]
    }

    override fun <KEY, DATA> setFetcher(key: KEY, fetcher: (suspend (key: KEY) -> DATA)) {
        (fetcherMap as MutableMap<KEY, (suspend (key: KEY) -> DATA)>)[key] = fetcher
    }

    override fun <KEY> getValidatedTimerJob(key: KEY): Job? {
        return (validatedTimeMap as MutableMap<KEY, Job>)[key]
    }

    override fun <KEY> setValidatedTimerJob(key: KEY, job: Job) {
        (validatedTimeMap as MutableMap<KEY, Job>)[key] = job
    }

    override fun <KEY> getFocusedTimerJob(key: KEY): Job? {
        return (focusedTimeMap as MutableMap<KEY, Job>)[key]
    }

    override fun <KEY> setFocusedTimerJob(key: KEY, job: Job) {
        (focusedTimeMap as MutableMap<KEY, Job>)[key] = job
    }

    override fun <KEY> getRetryingJobSet(key: KEY): Set<Job> {
        return (isRetryingJobSetMap as MutableMap<KEY, Set<Job>>)[key] ?: emptySet()
    }

    override fun <KEY> setRetryingJobSet(key: KEY, jobs: Set<Job>) {
        (isRetryingJobSetMap as MutableMap<KEY, Set<Job>>)[key] = jobs
    }

    override fun clear() {
        isValidatingStateMap.clear()
        errorStateMap.clear()
        fetcherMap.clear()
        validatedTimeMap.clear()
        focusedTimeMap.clear()
        isRetryingJobSetMap.clear()
    }
}
