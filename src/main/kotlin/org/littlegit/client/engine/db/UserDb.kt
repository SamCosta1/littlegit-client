package org.littlegit.client.engine.db

import org.littlegit.client.engine.model.User

class UserDb: LocalDb() {

    companion object {
        private const val DB_KEY = "current_user"
    }

    fun saveUser(user: User) {

    }

    fun getUser(): User? {
        return null
    }
}