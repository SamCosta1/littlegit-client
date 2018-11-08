package org.littlegit.client.engine.db

import org.littlegit.client.engine.model.AuthTokens
import org.littlegit.client.engine.util.SimpleCallback

open class AuthDb: LocalDb() {

    companion object {
        private const val DB_KEY = "auth_tokens"
    }

    fun getTokens(completion: (AuthTokens?) -> Unit) = readAsync(DB_KEY, AuthTokens::class.java, completion)
    open fun updateTokens(tokens: AuthTokens, completion: SimpleCallback<Unit>? = null) = writeAsync(DB_KEY, tokens, AuthTokens::class.java, completion)
    fun clearTokens(completion: SimpleCallback<Unit>? = null) = clear(DB_KEY, completion)
}
