package com.kazakago.swr.compose

import com.kazakago.swr.compose.internal.GlobalKonnection
import dev.tmapps.konnection.Konnection
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class NetworkRule : TestRule {

    private val konnection = mockk<Konnection>()
    private val connectionObserver = Channel<Boolean>(Channel.CONFLATED)

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    setup()
                    base.evaluate()
                } finally {
                    teardown()
                }
            }
        }
    }

    private fun setup() {
        every { konnection.isConnected() } returns true
        every { konnection.observeHasConnection() } returns connectionObserver.receiveAsFlow()
        GlobalKonnection = konnection
    }

    private fun teardown() {
    }

    fun changeNetwork(isConnected: Boolean) {
        every { konnection.isConnected() } returns isConnected
        connectionObserver.trySend(isConnected)
    }
}
