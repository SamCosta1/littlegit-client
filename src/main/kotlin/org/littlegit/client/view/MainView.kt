package org.littlegit.client.view

import org.littlegit.client.app.Styles
import org.littlegit.client.engine.i18n.Localizer
import org.littlegit.client.engine.model.I18nKey
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    val localizer: Localizer by inject()
    override val root = hbox {
        label(localizer[I18nKey.AppName]) {
            addClass(Styles.heading)
        }
    }
}