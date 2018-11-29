package org.littlegit.client.db

import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.littlegit.client.engine.db.LocalDb
import org.littlegit.client.engine.db.LocalDbAccessor
import org.littlegit.client.testUtils.BaseAsyncTest
import java.nio.file.Paths
import kotlin.reflect.KClass

open class BaseDbTests<T : LocalDb>(private val clazz: KClass<T>): BaseAsyncTest() {

    lateinit var db: T; private set


    @Before
    override fun setup() {
        super.setup()

        db = findInTestScope(clazz)

    }
}