package org.littlegit.client.engine.i18n

import org.littlegit.client.engine.controller.InitableController
import org.littlegit.client.engine.model.*
import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.engine.serialization.listAdapter
import tornadofx.*

class Localizer: Controller(), InitableController {

    private val defaultLanguage = Language.English
    private val moshiProvider: MoshiProvider by inject()

    private var translations: Map<LocalizationKey, LocalizedString> = emptyMap()
    private lateinit var defaultLanguageTranslations: Map<LocalizationKey, LocalizedString>

    private fun getTranslations(lang: Language): Map<LocalizationKey, LocalizedString> {
        val moshi = moshiProvider.moshi
        val adapter = moshi.listAdapter(LocalizedString::class.java)
        val jsonString = javaClass.getResource("/${lang.code}.json").readText()

        val stringsList = adapter.fromJson(jsonString) ?: emptyList()
        return stringsList.map { it.term to it }.toMap()
    }

    override fun onStart(onReady: (InitableController) -> Unit) {
        runAsync {
            getTranslations(defaultLanguage)
        } ui {
            defaultLanguageTranslations = it
            onReady(this)
        }
    }

    operator fun get(key: I18nKey): String = translations[key.key]?.definition
            ?: defaultLanguageTranslations[key.key]?.definition
            ?: key.key

}