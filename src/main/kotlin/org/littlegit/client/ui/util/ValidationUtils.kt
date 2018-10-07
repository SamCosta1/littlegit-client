package org.littlegit.client.ui.util

object ValidationUtils {

    // Simple validation for now
    fun validateEmail(email: String): Boolean {
        return email.length > 5
                && email.contains('@')
                && email.contains('.')
                && email.length < 50
    }

    fun validatePassword(password: String): Boolean {
        return password.isNotBlank()
    }
}