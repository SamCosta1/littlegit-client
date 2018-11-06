package org.littlegit.client.engine.model

data class LoginRequest(val email: String = "", val password: String = "")

data class LoginResponse (
        override val accessToken: String,
        val refreshToken: String,
        val scheme: String,
        val user: User
) : AuthResponse

data class User (
        val id: Int,
        val email: String,
        val firstName: String,
        val surname: String,
        val role: Long,
        val languageCode: String,
        val username: String
) {
    val language: Language = Language.fromLanguageCode(languageCode)
}

data class RefreshRequest (
        val refreshToken: String,
        val userId: Int
)

data class RefreshResponse (
        override val accessToken: String,
        val scheme: String,
        val expiry: Int
) : AuthResponse

data class SignupRequest (
        val email: String = "",
        val password: String = "",
        val firstName: String = "",
        val surname: String = "",
        val languageCode: String = "",
        val username: String = ""
)

data class AuthTokens(val accessToken: String = "", val refreshToken: String = "")

interface AuthResponse {
    val accessToken: String
}