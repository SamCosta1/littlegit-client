package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Label
import org.littlegit.client.ConflictsResolvedEvent
import org.littlegit.client.ResolveConflictView
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.ThemeColors
import tornadofx.*

class UpdateRemoteView: BaseView() {

    private lateinit var label: Label
    private val viewModel = ViewModel()
    private val solvingConflicts = viewModel.bind { SimpleBooleanProperty(false) }
    private val conflictsView: ResolveConflictView by inject()

    init {
      subscribe<ConflictsResolvedEvent> {
          littleGitCoreController.doNext {

              try {
                  repoController.commitAndPush(it)
                  runLater {
                      repoController.currentlyUpdating = false
                      close()
                  }
              } catch (e: Exception) {}
          }
      }
    }

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
                        conflictsView.conflicts = it.data
                        currentStage?.isMaximized = true
                        solvingConflicts.value = true
                    }
                }
            }
        }

        vbox {
            add(conflictsView.root)
            visibleWhen(solvingConflicts)
        }
    }

    override fun onDock() {
        super.onDock()
        repoController.currentlyUpdating = true
    }

}
