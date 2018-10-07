package org.littlegit.client.ui.view.startup.loginflow

import javafx.event.EventHandler
import javafx.scene.paint.Color.rgb
import org.littlegit.client.engine.model.Language
import org.littlegit.client.ui.util.imageView
import org.littlegit.client.ui.view.BaseView
import tornadofx.*

class ChooseLanguageView: BaseView() {


    override val root = vbox {

        listview(Language.all.observable()) {
            cellFormat { lang ->
                graphic = cache {
                    hbox {
                        imageView(lang.image)
                        label(lang.displayName) {
                            style {
                                textFill = rgb(1, 1, 1)
                            }
                        }
                        onMouseClicked = EventHandler {
                            localizer.updateLanguage(lang)
                            replaceWith(SignupView::class)
                        }
                    }
                }
            }
        }
    }
}