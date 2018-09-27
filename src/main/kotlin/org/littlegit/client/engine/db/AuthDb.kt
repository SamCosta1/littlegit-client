package org.littlegit.client.engine.db

import org.littlegit.client.engine.model.AuthTokens

class AuthDb: LocalDb() {

    companion object {
        private const val DB_KEY = "auth_tokens"
    }

    fun getTokens(completion: (AuthTokens?) -> Unit) = readAsync(DB_KEY, AuthTokens::class.java, completion)
    fun updateTokens(tokens: AuthTokens, completion: (() -> Unit)? = null) = writeAsync(DB_KEY, tokens, AuthTokens::class.java, completion)
}
