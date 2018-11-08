package org.littlegit.client.ui.app

import tornadofx.*

@Suppress("UNCHECKED_CAST")
class StateStore: Controller() {

    private val store: MutableMap<String, Any> = mutableMapOf()

    fun add(key: String, value: Double) {
        store[key] = value
    }

    fun <T>get(key: String): T? {
        return store[key] as? T?
    }
}