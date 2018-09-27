package org.littlegit.client.engine.model

typealias LocalizationKey = String
data class LocalizedString(val term: LocalizationKey, val definition: String)

enum class Language(val code: String) {
    English("en-GB"),
    Welsh("cy");

    fun fromLanguageCode(code: String) = Language.values().find { it.code.toLowerCase() == code.toLowerCase() }
}

enum class I18nKey(val key: String) {
    AppName("app_name"),
    InvalidEmail("invalid_email"),
    InvalidPassword("invalid_password"),
    FirstNameBlank("first_name_blank"),
    SurnameBlank("surname_blank"),
    LanguageCodeBlank("language_code_blank"),
    SurnameTooLong("surname_too_long"),
    FirstNameTooLong("first_name_too_long"),
    InvalidLanguageCode("invalid_language_code"),
    Response500Body("5xx_error"),
    EmailInUse("email_in_use"),
    UsernameInUse("username_in_use"),
    RepoNameBlank("repo_name_blank"),
    RepoNameTooLong("repo_name_too_long"),
    RepoNameInvalid("repo-name-invalid"),
    DescriptionTooLong("description_too_long"),
    InvalidCapacity("invalid_capacity"),
    UsernameBlank("blank_username"),
    InvalidUsername("invalid_username"),
    ValueAlreadyExists("value_already_exists"),
    InvalidUserId("invalid_user_id"),
    InvalidPublicKey("invalid_public_key"),
    Email("email"),
    Password("password"),
    Login("login"),
    Signup("signup"),
    Name("name"),
    Surname("surname"),
    Unknown("-");



    companion object {
        private val keys: Map<String, I18nKey> = I18nKey.values().map { it.key to it }.toMap()
        fun fromRaw(raw: String?): I18nKey = keys[raw] ?: Unknown
    }

}