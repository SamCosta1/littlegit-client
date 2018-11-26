package org.littlegit.client.db

import org.junit.Test
import org.littlegit.client.engine.db.SShDb
import kotlin.test.assertEquals

class SshDbTests: BaseDbTests<SShDb>(SShDb::class) {

    @Test
    fun testGetSetSshKeyPath_IsSuccessful() = runTest { completion ->
        val sshKeyPath = dbFolder.root.toPath()

        db.setSshKeyPath(sshKeyPath) {
            db.getSshKeyPath {
                assertEquals(sshKeyPath, it)

                completion()
            }
        }
    }

    @Test
    fun testGetSetSshKeyPath_WithoutCache_IsSuccessful() = runTest { completion ->
        val sshKeyPath = dbFolder.root.toPath()

        db.setSshKeyPath(sshKeyPath) {
            db.clearCache()
            db.getSshKeyPath {
                assertEquals(sshKeyPath, it)

                completion()
            }
        }
    }
}