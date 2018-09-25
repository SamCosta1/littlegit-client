package org.littlegit.client

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.littlegit.client.engine.db.LocalDb
import org.littlegit.client.engine.db.LocalDbAccessor
import org.littlegit.client.engine.model.RefreshRequest
import tornadofx.*
import java.nio.file.Paths
import kotlin.test.assertEquals

class LocalDbTests {

    private lateinit var localDb: LocalDb

    @Rule
    @JvmField var testFolder = TemporaryFolder()

    @Before
    fun setup() {
        val scope = Scope()
        scope.set(LocalDbAccessor(Paths.get(testFolder.root.canonicalPath, "temp.txt")))
        localDb = find(scope)
    }

    @Test
    fun testWriteObject() {
        val key = "testWriteObject"
        val objToWrite = RefreshRequest("a refresh token", 202)
        localDb.write(key, objToWrite, RefreshRequest::class.java)

        val retrieved = localDb.read(key, RefreshRequest::class.java)
        assertEquals(objToWrite, retrieved)
    }

    @Test
    fun testReadObject_WhenExists() {

    }

    @Test
    fun testReadObject_WhenDoesNotExist() {

    }
}