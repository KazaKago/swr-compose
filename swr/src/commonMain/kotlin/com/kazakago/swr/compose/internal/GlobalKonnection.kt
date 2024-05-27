package com.kazakago.swr.compose.internal

import androidx.annotation.VisibleForTesting
import dev.tmapps.konnection.Konnection

@VisibleForTesting
internal var GlobalKonnection: Konnection
    set(value) {
        internalKonnection = value
    }
    get() {
        var localKonnection = internalKonnection
        if (localKonnection == null) {
            localKonnection = Konnection.instance
            internalKonnection = localKonnection
        }
        return localKonnection
    }
private var internalKonnection: Konnection? = null
