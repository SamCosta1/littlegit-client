package org.littlegit.client.engine.serialization

import com.squareup.moshi.*
import tornadofx.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.littlegit.client.engine.model.I18nKey


class MoshiProvider: Controller() {
    val moshi: Moshi

    init {
        moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .add(I18nKeyAdapter())
                .build()
    }
}

class I18nKeyAdapter {
    @FromJson fun fromJson(raw: String?): I18nKey? {
        return I18nKey.fromRaw(raw)
    }

    @ToJson fun toJson(key: I18nKey?): String? = key?.key

}

fun <T>Moshi.listAdapter(clazz: Class<T>): JsonAdapter<List<T>> {
    val listMyData = Types.newParameterizedType(List::class.java, clazz)
    return this.adapter(listMyData)
}