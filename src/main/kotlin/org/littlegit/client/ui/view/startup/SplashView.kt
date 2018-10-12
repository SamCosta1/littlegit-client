package org.littlegit.client.ui.view.startup

import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.controller.StartupController
import org.littlegit.client.ui.util.NavigationUtils
import org.littlegit.client.ui.view.BaseView
import org.littlegit.client.ui.view.MainView
import org.littlegit.client.ui.view.startup.loginflow.ChooseLanguageView
import org.littlegit.client.ui.view.startup.loginflow.LoginView
import tornadofx.*

class SplashView : BaseView() {

    private val startupController: StartupController by inject()
    private val authController: AuthController by inject()

    override val root = vbox {
        style {
            padding = box(10.px)
            minWidth = 20.px
            minHeight = 20.px
        }
        label("Hello world")
    }

    override fun onBeforeShow() {
        super.onBeforeShow()

        runLater {
            startupController.onStartup {
                println("Splashview")
                if (authController.isLoggedIn) {
                    NavigationUtils.navigateFromLoginFlow(this@SplashView, repoController)
                } else {
                    replaceWith(ChooseLanguageView::class)
                }
            }
        }
    }
}
