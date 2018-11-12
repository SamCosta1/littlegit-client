package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Parent
import javafx.scene.control.Label
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.ThemeColors
import tornadofx.*
import tornadofx.Stylesheet.Companion.listView

class UpdateRemoteView: BaseView() {

    private lateinit var label: Label
    private val viewModel = ViewModel()
    private val solvingConflicts = viewModel.bind { SimpleBooleanProperty(false) }

    override val root = vbox {
        maxWidth = 100.0
        maxHeight = 100.0
        style {
            backgroundColor += ThemeColors.LightPrimary
        }

        label(localizer.observable(I18nKey.UpdateAvailable))
        label = label("Status")
        button(localizer.observable(I18nKey.UpdateToLatest)).action {
            repoController.updateToLatestFetched {
                if (!it.isError) {
                    if (it.data?.hasConflicts == false) {
                        repoController.currentlyUpdating = false
                        repoController.push()
                        close()
                    } else {
                        currentStage?.isMaximized = true
                        solvingConflicts.value = true
                    }
                }
            }
        }

        hbox {
            visibleWhen(solvingConflicts)
            vbox {

            }
        }
    }

    override fun onDock() {
        super.onDock()
        repoController.currentlyUpdating = true
    }

}
