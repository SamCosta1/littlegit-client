package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import org.littlegit.client.HideCommitView
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.app.ThemeColors
import org.littlegit.client.ui.util.Image
import org.littlegit.client.ui.util.format
import org.littlegit.client.ui.util.imageView
import org.littlegit.core.model.FileDiff
import org.littlegit.core.model.FullCommit
import org.littlegit.core.model.RawCommit
import org.littlegit.core.util.joinWithNewLines
import tornadofx.*

class ViewCommitView: BaseView() {

    private val viewModel = ViewModel()
    val isLoading = viewModel.bind { SimpleBooleanProperty()  }

    private lateinit var dateLabel: Label
    private lateinit var commitSubject: Label
    private lateinit var commitBody: Label
    private lateinit var youAddedContentLabel: Label
    private lateinit var youDeletedContentLabel: Label
    private lateinit var youModifiedContentLabel: Label

    private lateinit var youAddedLabel: Label
    private lateinit var youDeletedLabel: Label
    private lateinit var youModifiedLabel: Label

    var commit: RawCommit? = null; set(value) {
        if (commit == value) {
            return
        }

        field = value

        if (!isDocked || value == null) {
            return
        }

        updateCommit(value)
    }

    private fun updateCommit(value: RawCommit) {
        isLoading.value = true

        littleGitCoreController.doNext(false) {
            val result = it.repoReader.getFullCommit(value)
            runLater {
                isLoading.value = false

                if (!result.isError && result.data != null) {
                    bindCommit(result.data!!)
                }
            }
        }
    }

    private fun bindCommit(commit: FullCommit) {
        dateLabel.text = commit.date.format()
        commitSubject.text = commit.commitSubject
        commitBody.text = commit.commitBody.joinWithNewLines()

        val youAdded: String = commit.diff.fileDiffs.fold("") { acc, fileDiff ->
            if (fileDiff is FileDiff.NewFile) {
                "$acc\n${fileDiff.filePath.fileName}"
            } else {
                acc
            }
        }

        val youDeleted: String = commit.diff.fileDiffs.fold("") { acc, fileDiff ->
            if (fileDiff is FileDiff.DeletedFile) {
                "$acc\n${fileDiff.filePath.fileName }"
            } else {
                acc
            }
        }

        val youModified: String = commit.diff.fileDiffs.fold("") { acc, fileDiff ->
            when (fileDiff) {
                is FileDiff.RenamedFile -> "$acc\n${fileDiff.newPath.fileName}"
                is FileDiff.ChangedFile -> "$acc\n${fileDiff.filePath.fileName}"
                else -> acc
            }
        }

        youAddedContentLabel.text = youAdded
        youDeletedContentLabel.text = youDeleted
        youModifiedContentLabel.text = youModified

        youAddedLabel.isVisible = youAdded.isNotBlank()
        youAddedLabel.isManaged = youAdded.isNotBlank()

        youDeletedLabel.isVisible = youDeleted.isNotBlank()
        youDeletedLabel.isManaged = youDeleted.isNotBlank()

        youModifiedLabel.isVisible = youModified.isNotBlank()
        youModifiedLabel.isManaged = youModified.isNotBlank()

        youAddedContentLabel.isVisible = youAdded.isNotBlank()
        youAddedContentLabel.isManaged = youAdded.isNotBlank()

        youDeletedContentLabel.isVisible = youDeleted.isNotBlank()
        youDeletedContentLabel.isManaged = youDeleted.isNotBlank()

        youModifiedContentLabel.isVisible = youModified.isNotBlank()
        youModifiedContentLabel.isManaged = youModified.isNotBlank()
    }

    override val root = vbox {
        vgrow = Priority.ALWAYS

        spacing = 10.0
        hbox {
            dateLabel = label {
                alignment = Pos.CENTER_LEFT
                style {
                    fontSize = 16.px
                    textFill = ThemeColors.TransparentText
                }
            }

            spacer {
                hgrow = Priority.ALWAYS
            }

            imageView(Image.IcClose) {
                alignment = Pos.CENTER_RIGHT
                addClass(Styles.hover)
                onMouseClicked = EventHandler {
                    fire(HideCommitView)
                }
            }
        }

        commitSubject = label {
            isWrapText = true
            style {
                fontSize = 18.px
            }
        }

        commitBody = label {
            isWrapText = true
            style {
                fontSize = 16.px
            }
        }

        label(localizer.observable(I18nKey.YouChangedFiles)).addClass(Styles.transparentTitle)
        youAddedLabel = label(localizer.observable(I18nKey.YouAdded)).addClass(Styles.transparentTitle)
        youAddedContentLabel = label {
            style {
                textFill = c(141, 219, 55)
            }
        }

        youDeletedLabel = label(localizer.observable(I18nKey.YouDeleted)).addClass(Styles.transparentTitle)
        youDeletedContentLabel = label {
            style {
                textFill = c(219, 55, 55)
            }
        }

        youModifiedLabel = label(localizer.observable(I18nKey.YouModified)).addClass(Styles.transparentTitle)
        youModifiedContentLabel = label {
            style {
                textFill = c(74, 144, 226)
            }
        }
    }

    override fun onDock() {
        super.onDock()

        commit?.let { updateCommit(it) }
    }
}