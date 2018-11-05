package org.littlegit.client.db

import org.junit.Test
import org.littlegit.client.engine.db.AuthDb
import org.littlegit.client.engine.model.AuthTokens
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthDbTests: BaseDbTests<AuthDb>(AuthDb::class) {

    @Test
    fun testGetUpdateClearAuthTokens() = runTest { completion ->

        val tokens = AuthTokens("accessToken", "refreshToken")

        db.clearTokens()

        // Insert the tokens
        db.updateTokens(tokens) {

            // Retrieve them
            db.getTokens {  retrievedTokens ->
                assertEquals(tokens, retrievedTokens)

                // Clear them
                db.clearTokens {

                    // Ensure they're cleared
                    db.getTokens {
                        assertNull(it)
                        completion.invoke()
                    }
                }
            }
        }
    }
}