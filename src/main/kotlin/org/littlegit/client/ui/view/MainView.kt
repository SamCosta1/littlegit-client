package org.littlegit.client.ui.view

import javafx.scene.layout.Priority
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.view.startup.loginflow.ChooseLanguageView
import tornadofx.*

class MainView : BaseView(fullScreen = true) {
    private val authController: AuthController by inject()

    override val root = hbox {
        vbox {
            hgrow = Priority.ALWAYS
            hbox {
                prefHeight = 50.0
                spacing = 10.0

                addClass(Styles.primaryBackground)
                stackpane {
                    label(repoController.currentRepoNameObservable).addClass(Styles.heading)
                }
                button(localizer.observable(I18nKey.ChangeProject)).action {
                    replaceWith(ChooseRepoView::class)
                }
            }
        }
        vbox {
            prefWidth = 300.0
            addClass(Styles.primaryBackground)

            button(localizer.observable(I18nKey.Logout)).action {
                authController.logout()
                replaceWith(ChooseLanguageView::class)
            }
            button(localizer.observable(I18nKey.CommitAll)).action {
                repoController.stageAllAndCommit()
            }

        }

    }

}