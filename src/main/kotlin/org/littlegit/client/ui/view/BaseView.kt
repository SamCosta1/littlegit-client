package org.littlegit.client.ui.view

import org.littlegit.client.engine.i18n.Localizer
import tornadofx.*

abstract class BaseView(title: String? = null, private val fullScreen: Boolean = false) : View(title ?: "") {
    protected val localizer: Localizer by inject()

    override fun onDock() {
        super.onDock()
        currentStage?.isMaximized = fullScreen
    }
}
