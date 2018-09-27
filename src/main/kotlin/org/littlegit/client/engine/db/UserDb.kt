package org.littlegit.client.engine.db

import org.littlegit.client.engine.model.User

class UserDb: LocalDb() {

    companion object {
        private const val DB_KEY = "current_user"
    }

    fun saveUser(user: User) {
        writeAsync(DB_KEY, user, User::class.java)
    }

    fun getUser(completion: (User?) -> Unit) = readAsync(DB_KEY, User::class.java, completion)
}