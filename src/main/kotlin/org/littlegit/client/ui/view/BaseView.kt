package org.littlegit.client.ui.view

import org.littlegit.client.engine.i18n.Localizer
import tornadofx.*

abstract class BaseView(title: String? = null) : View(title ?: "") {
    protected val localizer: Localizer by inject()
}
