package org.littlegit.client.testUtils

import org.awaitility.kotlin.await
import java.util.concurrent.TimeUnit

abstract class BaseAsyncTest {

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