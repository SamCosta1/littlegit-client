package org.littlegit.client.view

import org.littlegit.client.engine.i18n.Localizer
import tornadofx.*

class LoginView : View("My View") {
    val localizer: Localizer by inject()

    override val root = vbox {
        form {

        }
    }
}
