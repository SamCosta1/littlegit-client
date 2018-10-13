package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ModifiableObservableListBase
import javafx.collections.ObservableList
import javafx.event.EventHandler
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.model.Repo
import org.littlegit.client.ui.app.Main
import org.littlegit.client.ui.app.Styles
import tornadofx.*
import java.io.File

class ChooseRepoView : BaseView() {

    private val repos: ObservableList<Repo> = mutableListOf<Repo>().observable()
    private val model = ViewModel()
    private val isLoading = model.bind { SimpleBooleanProperty() }

    override val root = vbox {
        label(localizer.observable(I18nKey.ChooseRepo)) {
            addClass(Styles.heading)
        }
        button(localizer.observable(I18nKey.OpenNewProject)) {
            disableWhen(isLoading)
            action {
                isLoading.value = true
                chooseDirectory()?.let {
                    repoController.setCurrentRepo(it, this@ChooseRepoView::onRepoChosen)
                }
            }
        }

        listview(repos) {
            disableWhen(isLoading)
            cellFormat { repo ->
                graphic = cache {
                    vbox {
                        label(repo.path.fileName.toString())

                        onMouseClicked = EventHandler {
                            repoController.setCurrentRepo(repo, this@ChooseRepoView::onRepoChosen)
                        }
                    }

                }
            }
        }
    }

    private fun onRepoChosen(success: Boolean) {
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
