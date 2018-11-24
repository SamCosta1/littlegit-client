package org.littlegit.client.ui.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Parent
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.littlegit.client.CreateCommitEvent
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.ui.app.Styles
import tornadofx.*

class CommitView: BaseView() {

    private lateinit var textArea: TextArea
    private val model = ViewModel()
    private val isLoading = model.bind { SimpleBooleanProperty(false) }

    override val root = vbox {
        vgrow = Priority.ALWAYS
        textArea = textarea {
            promptText = localizer[I18nKey.CommitPromptText]
            isWrapText = true
            style {
                backgroundColor += Color.TRANSPARENT
            }
        }


        spacing = 10.0
        button(localizer.observable(I18nKey.CommitAll)) {
            useMaxWidth = true
            disableWhen(isLoading)
            action {
                isLoading.value = true
                fire(CreateCommitEvent(textArea.text))
            }
        }

        vbox {
            label(localizer.observable(I18nKey.WhatsThis)).addClass(Styles.heading)
            vbox {
                spacing = 15.0
                label(localizer.observable(I18nKey.WriteAMessage)).addClass(Styles.bulletText)
                label(localizer.observable(I18nKey.WeBackupChanges)).addClass(Styles.bulletText)
                label(localizer.observable(I18nKey.SeeInHistory)).addClass(Styles.bulletText)
            }
        }
    }

    init {
        localizer.addListener {
            textArea.promptText = localizer[I18nKey.CommitPromptText]
        }
    }

    fun notifyCommitFinished() {
        textArea.clear()
        isLoading.value = false
        root.requestFocus()
    }

}