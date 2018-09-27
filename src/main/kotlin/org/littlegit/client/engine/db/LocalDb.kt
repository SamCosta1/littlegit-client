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
        dbLocation = Paths.get(System.getProperty("user.dir"), ".db")
        if (!dbLocation.toFile().exists()) {
            dbLocation.toFile().mkdirs()
        }
        dbLocation = dbLocation.resolve(dbFileName)
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

    private fun readRawJSON(key: String): String?  {
        synchronized(dbAccessor) {
            dbAccessor.getDb().use { db ->
                db.map.use {
                    return it[key]
                }
            }
        }
    }

    fun <T>read(key: String, clazz: Class<T>): T? {
        val rawJSON = readRawJSON(key) ?: return null
        return moshi.adapter(clazz).fromJson(rawJSON)
    }

    fun <T>readList(key: String, clazz: Class<T>): List<T>? {
        val rawJSON = readRawJSON(key) ?: return null
        return moshi.listAdapter(clazz).fromJson(rawJSON)
    }

    private fun writeRawJSON(key: String, json: String) {
        synchronized(dbAccessor) {
            dbAccessor.getDb().use { db ->
                db.map.use {
                    it[key] = json
                    db.commit()
                }
            }
        }
    }

    fun <T>write(key: String, obj: T, clazz: Class<T>) {
        writeRawJSON(key, moshi.adapter(clazz).toJson(obj))
    }

    fun <T>writeList(key: String, obj: List<T>, clazz: Class<T>) {
        writeRawJSON(key, moshi.listAdapter(clazz).toJson(obj))
    }

    fun <T>readAsync(key: String, clazz: Class<T>, completion: (T?) -> Unit) = runAsync {
        read(key, clazz)
    } ui {
        completion(it)
    }

    fun <T>writeAsync(key: String, obj: T, clazz: Class<T>, completion: (() -> Unit)? = null) = runAsync {
        write(key, obj, clazz)
    } ui {
        completion?.invoke()
    }

    fun <T>readListAsync(key: String, clazz: Class<T>, completion: (List<T>?) -> Unit) = runAsync {
        readList(key, clazz)
    } ui {
        completion(it)
    }

    fun <T>writeListAsync(key: String, obj: List<T>, clazz: Class<T>, completion: (() -> Unit)? = null) = runAsync {
        writeList(key, obj, clazz)
    } ui {
        completion?.invoke()
    }
}


val DB.map; get() = this.hashMap("main-map", Serializer.STRING, Serializer.STRING).createOrOpen()