package org.littlegit.client

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.util.Image
import org.littlegit.client.ui.util.imageView
import org.littlegit.client.ui.view.BaseView
import org.littlegit.client.ui.view.FileView
import org.littlegit.core.model.ConflictFileType
import org.littlegit.core.model.LittleGitFile
import org.littlegit.core.model.MergeResult
import org.littlegit.core.util.joinWithNewLines
import tornadofx.*

class ResolveConflictView: BaseView() {

    private val file1View = FileView()
    private val file2View = FileView()
    private lateinit var outputField: TextArea
    private val tickSize = 25.0

    var valid = ViewModel().bind { SimpleBooleanProperty(false) }

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
        valid.value = false
        outputField.text = ""
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

    override val root = vbox {
        vgrow = Priority.ALWAYS
        addClass(Styles.primaryPadding)
        label(localizer.observable(I18nKey.ClashDetected)).addClass(Styles.heading)
        label(localizer.observable(I18nKey.ConflictMessage))

        spacing = 10.0
        hbox {

            vbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.BOTTOM_CENTER

                label(localizer.observable(I18nKey.Version1))
                add(file1View)


                imageView(Image.ICTick) {
                    addClass(Styles.handOnHover)
                    fitHeight = tickSize
                    fitWidth = tickSize
                    onMouseClicked = EventHandler {
                        outputField.text = file1View.file?.content?.joinWithNewLines() ?: ""
                        valid.value = true
                    }
                }
            }

            vbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.BOTTOM_CENTER

                label(localizer.observable(I18nKey.Version2))
                add(file2View)


                imageView(Image.ICTick) {
                    addClass(Styles.handOnHover)
                    fitHeight = tickSize
                    fitWidth = tickSize

                    onMouseClicked = EventHandler {
                        outputField.text = file2View.file?.content?.joinWithNewLines() ?: ""
                        valid.value = true
                    }
                }
            }

            vbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.BOTTOM_CENTER

                label(localizer.observable(I18nKey.FinalVersion))

                outputField = textarea() {
                    vgrow = Priority.ALWAYS
                }
                enableWhen(valid)
                button(localizer.observable(I18nKey.Finish)).action {
                    val newFile = LittleGitFile(outputField.text.split("\n"), file1View.file!!.file)
                    repoController.writeAndStage(newFile) {
                        currentFileIndex += 1
                    }
                }
            }
        }
    }
}