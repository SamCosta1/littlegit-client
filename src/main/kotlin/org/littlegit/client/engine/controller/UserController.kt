package org.littlegit.client.engine.controller

import org.littlegit.client.engine.db.UserDb
import org.littlegit.client.engine.model.User
import tornadofx.*

class UserController: Controller(), InitableController {

    var currentUser: User? = null
    private val userDb: UserDb by inject()

    override fun onStart(onReady: (InitableController) -> Unit) {
        userDb.getUser {
            currentUser = it
            onReady(this)
        }
    }

    fun updateUserCache(user: User) {
        currentUser = user
        userDb.saveUser(user)
    }

}
