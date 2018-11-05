package org.littlegit.client.testUtils

import org.awaitility.kotlin.await
import org.junit.Before
import org.littlegit.client.engine.db.LocalDbAccessor
import tornadofx.*
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

abstract class BaseAsyncTest {

     lateinit var scope: Scope

    @Before
    open fun setup() {
        scope = Scope()
        com.sun.javafx.application.PlatformImpl.startup { }
    }

    fun <T : ScopedInstance>addToScope(value: T, clazz: KClass<T>) {
        setInScope(value, scope, clazz)
    }

    fun <T: Component>findInTestScope(clazz: KClass<T>): T {
        return find(clazz, scope)
    }

    fun runTest(test: (isComplete: () -> Unit) -> Unit) {
        var done = false

        test {
            done = true
        }

        await.pollInterval(10, TimeUnit.MILLISECONDS).until {
            done
        }
    }
}