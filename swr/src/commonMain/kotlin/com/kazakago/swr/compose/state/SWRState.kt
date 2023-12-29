package com.kazakago.swr.compose.state

import androidx.compose.runtime.Immutable
import com.kazakago.swr.compose.mutate.SWRMutate

public interface SWRState<KEY, DATA> {
    public val data: DATA?
    public val error: Throwable?
    public val isLoading: Boolean
    public val isValidating: Boolean
    public val mutate: SWRMutate<KEY, DATA>

    public operator fun component1(): DATA? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isLoading
    public operator fun component4(): Boolean = isValidating
    public operator fun component5(): SWRMutate<KEY, DATA> = mutate

    public companion object {
        public fun <KEY, DATA> empty(
            data: DATA? = null,
            error: Throwable? = null,
            isLoading: Boolean = false,
            isValidating: Boolean = false,
            mutate: SWRMutate<KEY, DATA> = SWRMutate.empty(),
        ): SWRState<KEY, DATA> = SWRStateImpl(data = data, error = error, isLoading = isLoading, isValidating = isValidating, mutate = mutate)
    }
}

@Immutable
internal data class SWRStateImpl<KEY, DATA>(
    override val data: DATA?,
    override val error: Throwable?,
    override val isLoading: Boolean,
    override val isValidating: Boolean,
    override val mutate: SWRMutate<KEY, DATA>,
) : SWRState<KEY, DATA>
