package org.littlegit.client.ui.view

import javafx.scene.Parent
import javafx.scene.control.Label
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.ThemeColors
import tornadofx.*

class UpdateRemoteView: BaseView() {

    private lateinit var label: Label
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
                    }
                   label.text = if (it.data?.hasConflicts == false) "No Conflicts yey!" else  it.data?.conflictFiles.toString()
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        repoController.currentlyUpdating = true
    }

}
