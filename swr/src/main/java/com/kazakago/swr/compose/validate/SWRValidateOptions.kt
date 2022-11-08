package com.kazakago.swr.compose.validate

import androidx.compose.runtime.Immutable

@Immutable
public data class SWRValidateOptions(
    public val retryCount: Int,
    public val dedupe: Boolean,
)
