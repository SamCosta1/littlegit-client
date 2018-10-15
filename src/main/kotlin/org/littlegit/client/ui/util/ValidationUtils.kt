package org.littlegit.client.ui.util

import org.littlegit.client.engine.model.I18nKey

object ValidationUtils {

    // Simple validation for now
    fun validateEmail(email: String): Boolean {
        return email.isNotBlank()
    }

    fun validatePassword(password: String): Boolean {
        return password.isNotBlank()
    }
}

/**
 * If any of the given list is contained in this list, then return one of them
 * otherwise return null
 */
fun List<I18nKey>.findOneOf(toFind: List<I18nKey>): I18nKey? = this.intersect(toFind).firstOrNull()
fun List<I18nKey>.findOneOf(vararg toFind: I18nKey): I18nKey? = findOneOf(toFind.toList())
