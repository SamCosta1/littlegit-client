package org.littlegit.client.ui.view

import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Label
import org.littlegit.client.UnauthorizedEvent
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.model.Repo
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.util.secondarylabel
import org.littlegit.client.ui.view.startup.loginflow.ChooseLanguageView
import org.littlegit.client.ui.view.startup.loginflow.LoginView
import tornadofx.*

class ChooseRepoView : BaseView() {

    private val authController: AuthController by inject()
    private val repos: ObservableList<Repo> = mutableListOf<Repo>().observable()
    private val model = ViewModel()
    private val isLoading = model.bind { SimpleBooleanProperty() }
    private lateinit var recentReposHeading: Label
    override val root = vbox {
        addClass(Styles.primaryBackground)
        padding = tornadofx.insets(10)
        spacing = 20.0

        label(localizer.observable(I18nKey.ChooseRepo)) {
            addClass(Styles.heading)
        }

        button(localizer.observable(I18nKey.OpenNewProject)) {
            disableWhen(isLoading)
            useMaxWidth = true
            action {
                isLoading.value = true
                val file = chooseDirectory()

                file?.let {
                    repoController.setCurrentRepo(it, this@ChooseRepoView::onRepoChosen)
                } ?: run {
                    isLoading.value = false
                }
            }
        }

        separator()

        recentReposHeading = label(localizer.observable(I18nKey.RecentRepos)) {
            addClass(Styles.subheading)
        }

        listview(repos) {
            disableWhen(isLoading)
            cellFormat { repo ->
                graphic = cache {
                    vbox {
                        addClass(Styles.cardView)
                        addClass(Styles.selectableCardView)
                        label(repo.path.fileName.toString())

                        stackpane {
                            minWidth = 0.0
                            prefWidth = 1.0
                            alignment = Pos.CENTER_LEFT
                            secondarylabel(repo.path.toAbsolutePath().toString())
                        }

                        onMouseClicked = EventHandler {
                            repoController.setCurrentRepo(repo, this@ChooseRepoView::onRepoChosen)
                        }
                    }

                }
            }
        }
    }

    private fun onRepoChosen(success: Boolean, repo: Repo) {
        if (success && repo.remoteRepo == null) {
            repoController.createRemoteRepo(repo) {
                moveToMainIfNeeded(it.isSuccess)
            }
        } else {
            moveToMainIfNeeded(success)
        }
    }

    private fun moveToMainIfNeeded(move: Boolean) {
        isLoading.value = false
        if (move) {
            replaceWith(MainView::class)
        } else {
            isLoading.value = false
        }
    }

    override fun onDock() {
        super.onDock()
        repoController.getRepos {
            repos.setAll(it ?: emptyList())
            recentReposHeading.isVisible = repos.isNotEmpty()
        }

    }

    private fun logout() {
        authController.logout()
        replaceWith(LoginView::class)
    }

    init {
        subscribe<UnauthorizedEvent> {
            logout()
        }
    }
}
