package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import javafx.util.Duration
import org.littlegit.client.*
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.controller.SShController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.app.ThemeColors
import org.littlegit.client.ui.app.graph.GraphView
import org.littlegit.client.ui.util.Image
import org.littlegit.client.ui.util.imageView
import org.littlegit.client.ui.view.startup.loginflow.ChooseLanguageView
import tornadofx.*

class MainView : BaseView(fullScreen = true) {
    private val animationDuration = Duration.millis(200.0)

    private val authController: AuthController by inject()
    private val sshController: SShController by inject()

    private val graphView: GraphView by inject()
    private val model = ViewModel()
    private val isLoading = model.bind { SimpleBooleanProperty(false) }

    private val commitView: CommitView by inject()
    private val viewCommitView: ViewCommitView by inject()
    override val root = vbox {

        style {
            backgroundColor += ThemeColors.Error
        }
        hbox {
            prefHeight = 50.0
            style {
                borderStyle += BorderStrokeStyle.SOLID
                borderWidth += box(0.px, 0.px, 2.px, 0.px)
                borderColor += box(ThemeColors.DarkPrimary1)
                backgroundColor += ThemeColors.LightPrimary
                cursor = Cursor.HAND
            }

            disableWhen(isLoading)
            addClass(Styles.primaryPadding)

            spacing = 10.0
            alignment = Pos.CENTER_LEFT
            label(repoController.currentRepoNameObservable).addClass(Styles.heading)
            imageView(Image.IcOpenRepo) {
                fitHeight = 15.0
                isPreserveRatio = true

                onMouseClicked = EventHandler {
                    replaceWith(ChooseRepoView::class)
                }
            }


            spacer {
                hgrow = Priority.ALWAYS
            }
            imageView(Image.IcLogout) {
                fitHeight = 25.0
                isPreserveRatio = true

                onMouseClicked = EventHandler {
                    logout()
                }
            }


        }

        hbox {

            vgrow = Priority.ALWAYS

            vbox {

                style {
                    borderStyle += BorderStrokeStyle.SOLID
                    borderWidth += box(0.px, 2.px, 0.px, 0.px)
                    borderColor += box(ThemeColors.DarkPrimary1)
                }

                prefWidth = 400.0
                addClass(Styles.primaryBackground)
                addClass(Styles.primaryPadding)

                // Is swapped out for view commit view
                add(commitView.root)
            }


            // Graph
            vbox {
                hgrow = Priority.ALWAYS
                style {
                    backgroundColor += ThemeColors.DarkPrimary3
                }

                add(graphView.root)
            }



        }
    }

    private fun logout() {
        authController.logout()
        replaceWith(ChooseLanguageView::class)
    }

    override fun onDock() {
        super.onDock()

        root.requestFocus()
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

        subscribe<UpdateAvailable> {
            repoController.loadLog()
            find(UpdateRemoteView::class).openWindow(StageStyle.UTILITY)
        }

        subscribe<CreateCommitEvent> { event ->
            isLoading.value = true
            repoController.stageAllAndCommit(event.message) {
                isLoading.value = false
                commitView.notifyCommitFinished()
            }
        }

        subscribe<HideCommitView> {
            if (viewCommitView.isDocked) {
                viewCommitView.replaceWith(commitView, ViewTransition.Slide(animationDuration))
            }
        }

        subscribe<ShowCommitEvent> { event ->

            if (!viewCommitView.isDocked) {
                commitView.replaceWith(viewCommitView, ViewTransition.Slide(animationDuration))
            }

            viewCommitView.commit = event.commit
        }

        subscribe<RepoNoLongerExistsEvent> {
            replaceWith<ChooseRepoView>()
        }
    }
}

