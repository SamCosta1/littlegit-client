package org.littlegit.client.testUtils

import org.awaitility.kotlin.await
import org.junit.Before
import java.util.concurrent.TimeUnit

abstract class BaseAsyncTest {

    @Before
    open fun setup() {
        com.sun.javafx.application.PlatformImpl.startup { }
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