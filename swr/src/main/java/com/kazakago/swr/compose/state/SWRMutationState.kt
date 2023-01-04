package com.kazakago.swr.compose.state

import androidx.compose.runtime.Immutable
import com.kazakago.swr.compose.mutate.SWRMutate
import com.kazakago.swr.compose.trigger.SWRReset
import com.kazakago.swr.compose.trigger.SWRTrigger

public interface SWRMutationState<KEY, DATA, ARG> {
    public val data: DATA?
    public val error: Throwable?
    public val trigger: SWRTrigger<KEY, DATA, ARG>
    public val reset: SWRReset<KEY, DATA>
    public val isMutating: Boolean

    public operator fun component1(): DATA? = data
    public operator fun component2(): Throwable? = error
    public operator fun component3(): SWRTrigger<KEY, DATA, ARG> = trigger
    public operator fun component4(): SWRReset<KEY, DATA> = reset
    public operator fun component5(): Boolean = isMutating

    public companion object {
        public fun <KEY, DATA, ARG> empty(
            data: DATA? = null,
            error: Throwable? = null,
            trigger: SWRTrigger<KEY, DATA, ARG> = SWRTrigger.empty(),
            reset: SWRReset<KEY, DATA> = SWRReset.empty(),
            isMutating: Boolean = false,
        ): SWRMutationState<KEY, DATA, ARG> = SWRMutationStateImpl(data = data, error = error, trigger = trigger, reset = reset, isMutating = isMutating)
    }
}

@Immutable
internal data class SWRMutationStateImpl<KEY, DATA, ARG>(
    override val data: DATA?,
    override val error: Throwable?,
    override val trigger: SWRTrigger<KEY, DATA, ARG>,
    override val reset: SWRReset<KEY, DATA>,
    override val isMutating: Boolean,
) : SWRMutationState<KEY, DATA, ARG>
