package org.littlegit.client.ui.view.startup

import javafx.geometry.Pos
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.controller.StartupController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.util.Image
import org.littlegit.client.ui.util.NavigationUtils
import org.littlegit.client.ui.util.secondarylabel
import org.littlegit.client.ui.view.BaseView
import org.littlegit.client.ui.view.MainView
import org.littlegit.client.ui.view.startup.loginflow.ChooseLanguageView
import org.littlegit.client.ui.view.startup.loginflow.LoginView
import tornadofx.*

class SplashView : BaseView() {

    private val startupController: StartupController by inject()
    private val authController: AuthController by inject()

    override val root = borderpane {
        addClass(Styles.primaryBackground)

        center {
            vbox {
                alignment = Pos.CENTER
                label("Logo goes here")
                label("LittleGit").addClass(Styles.heading)
                secondarylabel("Loading")
            }
        }

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
