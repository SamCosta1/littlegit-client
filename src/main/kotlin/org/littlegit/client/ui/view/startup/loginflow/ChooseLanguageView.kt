package org.littlegit.client.ui.view.startup.loginflow

import javafx.event.EventHandler
import javafx.scene.text.TextAlignment
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.model.Language
import org.littlegit.client.ui.app.Styles
import org.littlegit.client.ui.app.ThemeColors
import org.littlegit.client.ui.util.imageView
import org.littlegit.client.ui.view.BaseView
import tornadofx.*

class ChooseLanguageView: BaseView(fullScreen = false) {


    override val root = vbox {
        addClass(Styles.primaryBackground)
        padding = tornadofx.insets(10)
        spacing = 50.0
        label(localizer.observable(I18nKey.ChooseLanguage)) {
            addClass(Styles.heading)
        }

        listview(Language.all.observable()) {

            cellFormat { lang ->
                graphic = cache {
                    stackpane {
                        addClass(Styles.cardView)
                        addClass(Styles.selectableCardView)

                        style {
                            textAlignment = TextAlignment.CENTER
                        }

                        borderpane().left {
                            imageView(lang.image)
                        }

                        borderpane().center {
                            label(lang.displayName).style {
                                textFill = ThemeColors.PrimaryText
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