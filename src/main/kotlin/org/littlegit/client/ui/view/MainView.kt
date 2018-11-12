package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Priority
import org.littlegit.client.UnauthorizedEvent
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.controller.SShController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.app.ThemeColors
import org.littlegit.client.ui.app.graph.GraphView
import org.littlegit.client.ui.view.startup.loginflow.ChooseLanguageView
import tornadofx.*

class MainView : BaseView(fullScreen = true) {
    private val authController: AuthController by inject()
    private val sshController: SShController by inject()

    private val graphView: GraphView by inject()
    private val model = ViewModel()
    private val isLoading = model.bind { SimpleBooleanProperty(false) }

    private lateinit var textArea: TextArea

    override val root = hbox {
        vbox {
            hgrow = Priority.ALWAYS
            hbox {
                prefHeight = 50.0
                spacing = 10.0

                style {
                    borderStyle += BorderStrokeStyle.SOLID
                    borderWidth += box(0.px, 0.px, 2.px, 0.px)
                    borderColor += box(ThemeColors.DarkPrimary1)
                }
                addClass(Styles.primaryBackground)
                stackpane {
                    label(repoController.currentRepoNameObservable).addClass(Styles.heading)
                }

                button(localizer.observable(I18nKey.ChangeProject)).action {
                    disableWhen(isLoading)
                    replaceWith(ChooseRepoView::class)
                }
            }

            // Graph
            vbox {
                vgrow = Priority.ALWAYS
                style {
                    backgroundColor += ThemeColors.Primary
                }
                add(graphView.root)
            }
        }
        vbox {
            prefWidth = 300.0
            addClass(Styles.primaryBackground)

            style {
                borderStyle += BorderStrokeStyle.SOLID
                borderWidth += box(0.px, 0.px, 0.px, 2.px)
                borderColor += box(ThemeColors.DarkPrimary1)
            }

            stackpane {
                alignment = Pos.CENTER_RIGHT
                button(localizer.observable(I18nKey.Logout)).action {
                    logout()
                }
            }

            textArea = textarea()

            button(localizer.observable(I18nKey.CommitAll)).action {
                disableWhen(isLoading)
                isLoading.value = true

                repoController.stageAllAndCommit(textArea.text) {
                    textArea.text = ""
                    isLoading.value = false
                }
            }

        }
    }

    private fun logout() {
        authController.logout()
        replaceWith(ChooseLanguageView::class)
    }

    override fun onDock() {
        super.onDock()

        sshController.checkSshKeysExist { exist ->
            if (!exist) {
                sshController.generateAndAddSshKey {
                    if (!it.isSuccess) {
                        // TODO: Warn the user something went wrong
                    }
                }
            }
        }

    }

    init {
        subscribe<UnauthorizedEvent> {
            logout()
        }
    }

}