package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ModifiableObservableListBase
import javafx.collections.ObservableList
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.model.Repo
import org.littlegit.client.ui.app.Main
import org.littlegit.client.ui.app.Styles
import tornadofx.*

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
                    repoController.setCurrentRepo(it) {
                        if (it) {
                            replaceWith(MainView::class)
                        } else {
                            isLoading.value = false
                        }
                    }
                }
            }
        }

        listview(repos) {
            disableWhen(isLoading)
            cellFormat { repo ->
                graphic = cache {
                    label(repo.path.fileName.toString())
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        repoController.getRepos {
            repos.setAll(it ?: emptyList())
        }

    }
}
