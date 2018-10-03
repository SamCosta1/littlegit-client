package org.littlegit.client.engine.controller

import org.littlegit.client.engine.db.UserDb
import org.littlegit.client.engine.i18n.Localizer
import org.littlegit.client.engine.model.User
import tornadofx.*

class UserController: Controller(), InitableController {

    var currentUser: User? = null; private set

    private val userDb: UserDb by inject()
    private val localizer: Localizer by inject()

    override fun onStart(onReady: (InitableController) -> Unit) {
        userDb.getUser {
            currentUser = it

            if (it?.language != null) {
                localizer.updateLanguage(it.language) {
                    onReady(this)
                }
            } else {
                onReady(this)
            }
        }
    }

    fun updateUserCache(user: User) {
        currentUser = user
        userDb.saveUser(user)
        localizer.updateLanguage(user.language)
    }

}
