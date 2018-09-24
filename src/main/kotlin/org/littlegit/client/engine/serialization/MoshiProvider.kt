package org.littlegit.client.engine.serialization

import com.squareup.moshi.Moshi
import tornadofx.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class MoshiProvider: Controller() {
    val moshi: Moshi

    init {
        moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
    }
}

fun <T>Moshi.listAdapter(clazz: Class<T>): JsonAdapter<List<T>> {
    val listMyData = Types.newParameterizedType(List::class.java, clazz)
    return this.adapter(listMyData)
}