package org.littlegit.client.view

import org.littlegit.client.app.Styles
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.view.startup.loginflow.LoginView
import tornadofx.*

class MainView : BaseView() {
    private val authController: AuthController by inject()

    override val root = hbox {
        label(localizer[I18nKey.AppName]) {
            addClass(Styles.heading)
        }
        button(localizer.observable(I18nKey.Logout)).action {
            authController.logout()
            replaceWith(LoginView::class)
        }
    }
}