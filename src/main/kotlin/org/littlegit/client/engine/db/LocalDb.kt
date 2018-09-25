package org.littlegit.client.engine.db

import com.squareup.moshi.Moshi
import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.engine.serialization.listAdapter
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths

class LocalDbAccessor(): Controller() {
    private val dbFileName = javaClass.canonicalName
    private var dbLocation: Path

    init{
        dbLocation = Paths.get(System.getProperty("user.dir"), ".db", dbFileName)
    }

    // Only used for testing
    constructor(dbLocation: Path): this() {
        this.dbLocation = dbLocation
    }

    fun getDb(): DB {
        return DBMaker
                .fileDB(dbLocation.toFile())
                .transactionEnable()
                .make()
    }
}

open class LocalDb: Controller() {
    private val moshiProvider: MoshiProvider by inject()
    private val moshi: Moshi; get() = moshiProvider.moshi

    private val dbAccessor: LocalDbAccessor by inject()

    private fun readRawJSON(key: String): String = dbAccessor.getDb().use { db ->
        db.map.use {
            return  it[key] as String
        }
    }

    fun <T>read(key: String, clazz: Class<T>): T? {
        return moshi.adapter(clazz).fromJson(readRawJSON(key))
    }

    fun <T>readList(key: String, clazz: Class<T>): List<T>? {
        return moshi.listAdapter(clazz).fromJson(readRawJSON(key))
    }

    private fun writeRawJSON(key: String, json: String) {
        dbAccessor.getDb().use { db ->
            db.map.use {
                it[key] = json
                db.commit()
            }
        }
    }

    fun <T>write(key: String, obj: T, clazz: Class<T>) {
        writeRawJSON(key, moshi.adapter(clazz).toJson(obj))
    }

    fun <T>writeList(key: String, obj: List<T>, clazz: Class<T>) {
        writeRawJSON(key, moshi.listAdapter(clazz).toJson(obj))
    }
}


val DB.map; get() = this.hashMap("main-map", Serializer.STRING, Serializer.STRING).createOrOpen()