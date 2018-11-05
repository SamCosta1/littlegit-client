package org.littlegit.client.db

import javafx.application.Application
import javafx.application.Platform
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.littlegit.client.engine.db.LocalDb
import org.littlegit.client.engine.db.LocalDbAccessor
import org.littlegit.client.testUtils.BaseAsyncTest
import tornadofx.*
import java.nio.file.Paths
import kotlin.reflect.KClass

open class BaseDbTests<T : LocalDb>(private val clazz: KClass<T>): BaseAsyncTest() {

    lateinit var db: T; private set

    @Rule
    @JvmField var testFolder = TemporaryFolder()

    @Before
    override fun setup() {
        super.setup()

        addToScope(LocalDbAccessor(Paths.get(testFolder.root.canonicalPath, "temp.txt")), LocalDbAccessor::class)
        db = findInTestScope(clazz)

    }
}