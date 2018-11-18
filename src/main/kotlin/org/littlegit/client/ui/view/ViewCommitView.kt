package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.util.format
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
        field = value

        if (!isDocked || value == null) {
            return
        }

        updateCommit(value)

    }

    private fun updateCommit(value: RawCommit) {
        isLoading.value = true

        littleGitCoreController.doNext {
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

        youAddedContentLabel.isVisible = youAdded.isNotBlank()
        youDeletedContentLabel.isVisible = youDeleted.isNotBlank()
        youModifiedContentLabel.isVisible = youModified.isNotBlank()g
    }

    override val root = vbox {
        vgrow = Priority.ALWAYS

        dateLabel = label()
        commitSubject = label()
        commitBody = label()

        label(localizer.observable(I18nKey.YouChangedFiles))
        youAddedLabel = label(localizer.observable(I18nKey.YouAdded))
        youAddedContentLabel = label()

        youDeletedLabel = label(localizer.observable(I18nKey.YouDeleted))
        youDeletedContentLabel = label()

        youModifiedLabel = label(localizer.observable(I18nKey.YouModified))
        youModifiedContentLabel = label()
    }

    override fun onDock() {
        super.onDock()

        commit?.let { updateCommit(it) }
    }
}