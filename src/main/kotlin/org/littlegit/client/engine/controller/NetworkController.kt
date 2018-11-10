package org.littlegit.client.engine.controller

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*
import java.util.*
import java.net.InetSocketAddress
import java.net.Socket
import java.io.IOException
import java.net.UnknownHostException


class NetworkController: Controller() {

    val networkAvailability: SimpleBooleanProperty = SimpleBooleanProperty(false)

    private val timer = Timer()
    init {
        timer.schedule(object : TimerTask() {
            override fun run() {
                val available = isInternetAvailable()

                if (networkAvailability.value != available) {
                    networkAvailability.value = available
                }
            }

        }, 100, 15 * 1000)
    }

    // Credit: https://stackoverflow.com/questions/1402005/how-to-check-if-internet-connection-is-present-in-java
    @Throws(IOException::class)
    fun isInternetAvailable(): Boolean {
        return (isHostAvailable("google.com") || isHostAvailable("amazon.com")
                || isHostAvailable("facebook.com") || isHostAvailable("apple.com"))
    }

    @Throws(IOException::class)
    private fun isHostAvailable(hostName: String): Boolean {
        try {
            Socket().use { socket ->
                val port = 80
                val socketAddress = InetSocketAddress(hostName, port)
                socket.connect(socketAddress, 3000)

                return true
            }
        } catch (unknownHost: UnknownHostException) {
            return false
        }

    }
}