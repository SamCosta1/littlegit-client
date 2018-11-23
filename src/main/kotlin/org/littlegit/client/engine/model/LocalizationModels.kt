
package org.littlegit.client.engine.model

import org.littlegit.client.ui.util.Image

typealias LocalizationKey = String
data class LocalizedString(val term: LocalizationKey, val definition: String)

enum class Language(val code: kotlin.String, val displayName: String, val image: Image) {
    English("en-GB", "English", Image.EnglishFlag),
    Welsh("cy", "Cymraeg", Image.WelshFlag);


    companion object {
        val all = Language.values().asList()
        fun fromLanguageCode(code: String?) = all.find { it.code.toLowerCase() == code?.toLowerCase() } ?: English
    }
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
    Logout("logout"),
    NoNetworkConnection("no_network_connection"),
    ChooseRepo("choose_repo"),
    OpenNewProject("open_new_project"),
    CommitAll("commit_all"),
    ChangeProject("change_project"),
    ChooseLanguage("choose_language"),
    IncorrectLoginDetails("incorrect_login_details"),
    UnknownError("unknown_error"),
    RecentRepos("recent_repos"),
    QuickCommit("quick_commit"),
    UpdateAvailable("update_available"),
    UpdateToLatest("update_to_latest"),
    AutoCommitMessage("auto_commit_message"),
    CommitPromptText("commit_prompt_text"),
    WhatsThis("whats_this"),
    WeBackupChanges("backup_changes"),
    SeeInHistory("see_in_history"),
    WriteAMessage("write_message"),
    YouChangedFiles("you_changed_files"),
    YouAdded("you_added"),
    YouDeleted("you_deleted"),
    YouModified("you_modified"),
    Unknown("-");



    companion object {
        private val keys: Map<String, I18nKey> = I18nKey.values().map { it.key to it }.toMap()
        fun fromRaw(raw: String?): I18nKey = keys[raw] ?: Unknown
    }

}

