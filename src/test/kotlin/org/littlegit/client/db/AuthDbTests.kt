package org.littlegit.client.db

import org.awaitility.kotlin.await
import org.junit.Test
import org.littlegit.client.engine.db.AuthDb
import org.littlegit.client.engine.model.AuthTokens
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.test.assertFalse

class AuthDbTests: BaseDbTests<AuthDb>(AuthDb::class) {


    @Test
    fun testGetUpdateClearAuthTokens() = runTest { completion ->

        db.updateTokens(AuthTokens("ERg,","ERG")) {
            assertFalse(true)
            completion.invoke()
        }
    }
}