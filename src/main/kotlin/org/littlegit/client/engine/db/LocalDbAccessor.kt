package org.littlegit.client.engine.db

import com.squareup.moshi.Moshi
import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.engine.serialization.listAdapter
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import tornadofx.*
import java.nio.file.Paths

abstract class LocalDbAccessor: Controller() {
    private val moshiProvider: MoshiProvider by inject()
    private val moshi: Moshi; get() = moshiProvider.moshi

    private val dbFileName = javaClass.canonicalName
    private val dbLocation = Paths.get(System.getProperty("user.dir"), ".db", dbFileName)

    private fun getDb(): DB {
        return DBMaker
                .fileDB(dbLocation.toFile())
                .transactionEnable()
                .make()
    }

    private fun readRawJSON(key: String): String = getDb().use { db ->
        db.map.use {
            return  it[key] as String
        }
    }

    protected fun <T>read(key: String, clazz: Class<T>): T? {
        return moshi.adapter(clazz).fromJson(readRawJSON(key))
    }

    protected fun <T>readList(key: String, clazz: Class<T>): List<T>? {
        return moshi.listAdapter(clazz).fromJson(readRawJSON(key))
    }

    private fun writeRawJSON(key: String, json: String) {
        getDb().use { db ->
            db.map.use {
                it[key] = json
                db.commit()
            }
        }
    }

    protected fun <T>write(key: String, obj: T, clazz: Class<T>) {
        writeRawJSON(key, moshi.adapter(clazz).toJson(obj))
    }

    protected fun <T>writeList(key: String, obj: List<T>, clazz: Class<T>) {
        writeRawJSON(key, moshi.listAdapter(clazz).toJson(obj))
    }
}


val DB.map; get() = this.hashMap("main-map", Serializer.STRING, Serializer.STRING).createOrOpen()