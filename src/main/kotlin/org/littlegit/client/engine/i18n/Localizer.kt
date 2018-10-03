package org.littlegit.client.engine.i18n

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import org.littlegit.client.engine.controller.InitableController
import org.littlegit.client.engine.model.*
import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.engine.serialization.listAdapter
import tornadofx.*


typealias LanguageChangeListener = (Language) -> Unit
class Localizer: Controller(), InitableController {

    private val languageChangeListeners = mutableListOf<LanguageChangeListener>()

    private val defaultLanguage = Language.English
    private val moshiProvider: MoshiProvider by inject()

    private var translations: Map<LocalizationKey, LocalizedString> = emptyMap()
    private lateinit var defaultLanguageTranslations: Map<LocalizationKey, LocalizedString>

    private fun getTranslations(lang: Language): Map<LocalizationKey, LocalizedString> {
        val moshi = moshiProvider.moshi
        val adapter = moshi.listAdapter(LocalizedString::class.java)
        val jsonString = javaClass.getResource("/${lang.code.toLowerCase()}.json").readText()

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

    fun observable(key: I18nKey) = ObservableTranslation(key, this)

    fun updateLanguage(language: Language, completion: (() -> Unit)? = null) {
        runAsync {
            translations = getTranslations(language)
        } ui {
            languageChangeListeners.forEach { it.invoke(language) }
            completion?.invoke()
        }
    }

    fun addListener(listener: LanguageChangeListener) = languageChangeListeners.add(listener)
    fun removeListener(listener: LanguageChangeListener) = languageChangeListeners.remove(listener)
}

class ObservableTranslation(val key: I18nKey, val translationProvider: Localizer): ObservableValue<String> {

    private var currentValue = translationProvider[key]
    private val listeners = mutableListOf<ChangeListener<in String>?>()
    private val invalidationListeners = mutableListOf<InvalidationListener?>()

    init {
        translationProvider.addListener {
            val newTranslation = translationProvider[key]
            val oldValue = currentValue
            currentValue = newTranslation

            listeners.forEach { it?.changed(this, oldValue, newTranslation) }
            invalidationListeners.forEach { it?.invalidated(this) }
        }
    }

    override fun removeListener(listener: ChangeListener<in String>?) {
        listeners.remove(listener)
    }


    override fun addListener(listener: ChangeListener<in String>?) {
        listeners.add(listener)
    }

    override fun removeListener(listener: InvalidationListener?) {
        invalidationListeners.remove(listener)
    }

    override fun addListener(listener: InvalidationListener?) {
        invalidationListeners.add(listener)
    }

    override fun getValue(): String = currentValue

}

