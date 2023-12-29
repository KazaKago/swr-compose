package com.kazakago.swr.compose.preload

import androidx.compose.runtime.Immutable
import com.kazakago.swr.compose.validate.SWRValidate

public interface SWRPreload {
    public suspend operator fun invoke()
}

@Immutable
internal data class SWRPreloadImpl<KEY>(
    private val key: KEY?,
    private val validate: SWRValidate<KEY>,
) : SWRPreload {

    override suspend operator fun invoke() {
        validate(key)
    }
}
