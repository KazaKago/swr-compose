package com.kazakago.swr.compose.internal

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

internal var SWRGlobalScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
