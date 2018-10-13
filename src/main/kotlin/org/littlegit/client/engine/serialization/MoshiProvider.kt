package org.littlegit.client.engine.serialization

import com.squareup.moshi.*
import tornadofx.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.littlegit.client.engine.model.I18nKey
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class MoshiProvider: Controller() {
    val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(I18nKeyAdapter())
            .add(OffsetDateTimeTimeAdapter())
            .add(PathAdapter())
            .build()
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

class PathAdapter() {
    @ToJson fun toJson(path: Path?): String? {
        return path?.normalize().toString()
    }

    @FromJson fun fromJson(path: String): Path {
        return Paths.get(path)
    }
}
class OffsetDateTimeTimeAdapter {
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    @ToJson fun toJson(dateTime: OffsetDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }

    @FromJson fun fromJson(dateTime: String): OffsetDateTime {
        return OffsetDateTime.parse(dateTime, dateTimeFormatter).withNano(0)
    }
}