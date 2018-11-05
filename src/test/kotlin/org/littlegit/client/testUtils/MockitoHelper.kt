package org.littlegit.client.testUtils

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import kotlin.reflect.KClass

fun <T>upon(call: T): OngoingStubbing<T> = Mockito.`when`(call)

fun <T: Any>any(clazz: KClass<T>): T {
    ArgumentMatchers.any<T>()
    return clazz.java.newInstance()
}