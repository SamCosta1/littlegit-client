package org.littlegit.client.ui.view

import javafx.scene.Parent
import javafx.scene.layout.Priority
import org.littlegit.client.ui.app.Styles
import org.littlegit.core.model.LittleGitFile
import tornadofx.*

class FileView: Fragment() {

    var file: LittleGitFile? = null; set(value) {
        field = value

        lines.setAll(value?.content)
    }

    private val lines = mutableListOf<String>().observable()

    override val root = vbox {
        addClass(Styles.fileview)
        vgrow = Priority.ALWAYS
        listview(lines) {
            cellFormat { line ->
                graphic = cache {
                    label(line)
                }
            }
        }
    }
}