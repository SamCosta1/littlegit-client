package org.littlegit.client.ui.view.startup.loginflow

import javafx.event.EventHandler
import javafx.scene.paint.Color
import javafx.scene.paint.Color.rgb
import org.littlegit.client.engine.model.Language
import org.littlegit.client.ui.util.Image
import org.littlegit.client.ui.util.imageView
import org.littlegit.client.ui.view.BaseView
import tornadofx.*

class ChooseLanguageView: BaseView() {

    override val root = vbox {
        imageView(Image.WelshFlag) {
            setPrefSize(300.0, 240.0)
        }
        listview(Language.all.observable()) {
            cellFormat { lang ->

                    imageView(lang.image) {
                        setPrefSize(300.0, 240.0)
                    }
                    label(lang.displayName) {
                        style {
                            textFill = rgb(1,1,1)
                        }
                    }
                    println(lang.displayName)

                onMouseClicked = EventHandler {
                    println()
                }
            }
        }
    }
}