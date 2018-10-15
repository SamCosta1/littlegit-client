package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.model.Repo
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.util.secondarylabel
import tornadofx.*

class ChooseRepoView : BaseView() {

    private val repos: ObservableList<Repo> = mutableListOf<Repo>().observable()
    private val model = ViewModel()
    private val isLoading = model.bind { SimpleBooleanProperty() }

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
                chooseDirectory()?.let {
                    repoController.setCurrentRepo(it, this@ChooseRepoView::onRepoChosen)
                }
            }
        }

        separator()

        label(localizer.observable(I18nKey.RecentRepos)).addClass(Styles.subheading)

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

    private fun onRepoChosen(success: Boolean) {
        isLoading.value = false
        if (success) {
            replaceWith(MainView::class)
        } else {
            isLoading.value = false
        }
    }

    override fun onDock() {
        super.onDock()
        repoController.getRepos {
            repos.setAll(it ?: emptyList())
        }

    }
}
