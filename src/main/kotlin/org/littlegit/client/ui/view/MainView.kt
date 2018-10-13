package org.littlegit.client.ui.view

import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.view.startup.loginflow.ChooseLanguageView
import tornadofx.*

class MainView : BaseView(fullScreen = true) {
    private val authController: AuthController by inject()

    override val root = vbox {
        label(localizer[I18nKey.AppName]) {
            addClass(Styles.heading)
        }
        button(localizer.observable(I18nKey.Logout)).action {
            authController.logout()
            replaceWith(ChooseLanguageView::class)
        }
        button(localizer.observable(I18nKey.CommitAll)).action {
            repoController.stageAllAndCommit()
        }
        button(localizer.observable(I18nKey.ChangeProject)).action {
            replaceWith(ChooseRepoView::class)
        }
    }
}