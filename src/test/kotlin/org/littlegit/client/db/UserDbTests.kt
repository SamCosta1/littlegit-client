package org.littlegit.client.db

import org.junit.Test
import org.littlegit.client.engine.db.UserDb
import org.littlegit.client.testUtils.UserHelper
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserDbTests: BaseDbTests<UserDb>(UserDb::class) {

    @Test
    fun testSaveRetrieveUser_IsSuccessful() = runTest { completion ->
        val user = UserHelper.createUser(10)

        db.getUser { initialUser ->
            assertNull(initialUser)

            db.saveUser(user) {
                db.getUser { savedUser ->
                    assertEquals(user, savedUser)

                    // Make sure it can be overwritten
                    val newUser = UserHelper.createUser(11, "new name")
                    db.saveUser(newUser) {
                        db.getUser { updatedUser ->
                            assertEquals(newUser, updatedUser)

                            completion()
                        }
                    }
                }
            }

        }
    }
}