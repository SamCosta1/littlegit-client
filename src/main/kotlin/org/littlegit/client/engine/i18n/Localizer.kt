package org.littlegit.client.engine.i18n

import com.squareup.moshi.JsonReader
import okio.BufferedSource
import org.littlegit.client.engine.controller.ApiController
import org.littlegit.client.engine.model.*
import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.engine.serialization.listAdapter
import tornadofx.*

class Localizer: Controller() {

    private val moshiProvider: MoshiProvider by inject()

    private var translations: Map<LocalizationKey, LocalizedString>

    init {
        translations = getTranslations(Language.English)
    }

    private fun getTranslations(lang: Language): Map<LocalizationKey, LocalizedString> {
        val moshi = moshiProvider.moshi
        val adapter = moshi.listAdapter(LocalizedString::class.java)
        val jsonString = javaClass.getResource("/${lang.code}.json").readText()

        val stringsList = adapter.fromJson(jsonString) ?: emptyList()
        return stringsList.map { it.term to it }.toMap()
    }

    operator fun get(key: I18nKey): String = translations[key.key]?.definition ?: "-"
}