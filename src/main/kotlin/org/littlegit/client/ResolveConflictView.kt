package org.littlegit.client

import org.littlegit.client.ui.view.BaseView
import org.littlegit.client.ui.view.FileView
import org.littlegit.core.model.ConflictFileType
import org.littlegit.core.model.MergeResult
import tornadofx.*

class ResolveConflictView: BaseView() {

    val file1View = FileView()
    val file2View = FileView()


    var currentFileIndex = -1; set(value) {
        field = value

        if (conflicts == null) {
            return
        }

        if (currentFileIndex >= conflicts!!.conflictFiles.size) {
            fire(ConflictsResolvedEvent)
        } else {
            updateConflictFiles()
        }
    }

    private fun updateConflictFiles() {
        val conflictFile = conflicts?.conflictFiles?.get(currentFileIndex) ?: return
        littleGitCoreController.doNext {
            val file1 = it?.repoReader?.getConflictFileContent(conflictFile, ConflictFileType.Ours)
            val file2 = it?.repoReader?.getConflictFileContent(conflictFile, ConflictFileType.Theirs)
            runLater {
                file1View.file = file1?.data
                file2View.file = file2?.data
            }
        }
    }

    var conflicts: MergeResult? = null; set(value) {
        field = value
        if (field?.conflictFiles?.isEmpty() == false) {
            currentFileIndex = 0
        }
    }

    override val root = hbox {
        vbox {
            add(file1View)
            button("tick").action {
                repoController.writeAndStage(file1View.file) {
                    currentFileIndex += 1
                }
            }
        }

        vbox {
            add(file2View)
            button("tick").action {
                repoController.writeAndStage(file2View.file) {
                    currentFileIndex += 1
                }
            }
        }
    }
}