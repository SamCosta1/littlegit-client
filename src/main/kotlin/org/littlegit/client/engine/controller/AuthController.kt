package org.littlegit.client.engine.controller

import tornadofx.*

class AuthController : Controller(), InitableController {

    var isLoggedIn: Boolean = false

    override fun onStart(onReady: (InitableController) -> Unit) {
        // TODO: Read auth info from disk
        onReady(this)
    }
}