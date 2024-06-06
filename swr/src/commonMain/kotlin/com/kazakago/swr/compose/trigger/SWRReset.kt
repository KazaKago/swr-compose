package com.kazakago.swr.compose.trigger

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState

public interface SWRReset<KEY, DATA> {
    public operator fun invoke()

    public companion object {
        public fun <KEY, DATA> empty(): SWRReset<KEY, DATA> = object : SWRReset<KEY, DATA> {
            override fun invoke() {}
        }
    }
}

@Immutable
internal data class SWRResetImpl<KEY, DATA>(
    private val data: MutableState<DATA?>,
    private val error: MutableState<Throwable?>,
    private val isMutating: MutableState<Boolean>,
) : SWRReset<KEY, DATA> {

    override operator fun invoke() {
        data.value = null
        error.value = null
        isMutating.value = false
    }
}
