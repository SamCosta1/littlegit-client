package org.littlegit.client.engine.db

import org.littlegit.client.engine.model.User
import org.littlegit.client.engine.util.SimpleCallback

class UserDb: LocalDb() {

    companion object {
        private const val DB_KEY = "current_user"
    }

    fun saveUser(user: User, completion: SimpleCallback<Unit>? = null) {
        writeAsync(DB_KEY, user, User::class.java, completion)
    }

    fun getUser(completion: (User?) -> Unit) = readAsync(DB_KEY, User::class.java, completion)

    fun clearUser(completion: SimpleCallback<Unit>? = null) {
        clear(DB_KEY, completion)
    }
}