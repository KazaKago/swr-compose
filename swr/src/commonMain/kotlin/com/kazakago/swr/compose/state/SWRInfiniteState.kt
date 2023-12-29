package com.kazakago.swr.compose.state

import androidx.compose.runtime.Immutable
import com.kazakago.swr.compose.mutate.SWRMutate

public interface SWRInfiniteState<KEY, DATA> {
    public val data: List<DATA?>?
    public val error: Throwable?
    public val isLoading: Boolean
    public val isValidating: Boolean
    public val mutate: SWRMutate<KEY, List<DATA>>
    public val size: Int
    public val setSize: ((size: Int) -> Unit)

    public operator fun component1(): List<DATA?>? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): Boolean = isLoading
    public operator fun component4(): Boolean = isValidating
    public operator fun component5(): SWRMutate<KEY, List<DATA>> = mutate
    public operator fun component6(): Int = size
    public operator fun component7(): ((size: Int) -> Unit) = setSize

    public companion object {
        public fun <KEY, DATA> empty(
            data: List<DATA?> = emptyList(),
            error: Throwable? = null,
            isLoading: Boolean = false,
            isValidating: Boolean = false,
            mutate: SWRMutate<KEY, List<DATA>> = SWRMutate.empty(),
            size: Int = 1,
            setSize: (size: Int) -> Unit = {},
        ): SWRInfiniteState<KEY, DATA> = SWRInfiniteStateImpl(data = data, error = error, isLoading = isLoading, isValidating = isValidating, mutate = mutate, size = size, setSize = setSize)
    }
}

@Immutable
internal data class SWRInfiniteStateImpl<KEY, DATA>(
    override val data: List<DATA?>?,
    override val error: Throwable?,
    override val isLoading: Boolean,
    override val isValidating: Boolean,
    override val mutate: SWRMutate<KEY, List<DATA>>,
    override val size: Int,
    override val setSize: (size: Int) -> Unit,
) : SWRInfiniteState<KEY, DATA>
