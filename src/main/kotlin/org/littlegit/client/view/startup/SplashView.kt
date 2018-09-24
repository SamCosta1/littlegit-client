package org.littlegit.client.view.startup

import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.controller.StartupController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.view.BaseView
import org.littlegit.client.view.MainView
import org.littlegit.client.view.startup.loginflow.LoginView
import tornadofx.*

class SplashView : BaseView() {

    private val startupController: StartupController by inject()
    private val authController: AuthController by inject()

    override val root = vbox {
        style {
            padding = box(10.px)
        }
        label(localizer[I18nKey.AppName])
    }

    override fun onCreate() {
        super.onCreate()

        startupController.onStartup {
            if (authController.isLoggedIn) {
                replaceWith(MainView::class)
            } else {
                replaceWith(LoginView::class)
            }

        }
    }
}
