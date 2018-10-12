package org.littlegit.client.ui.view

import org.littlegit.client.engine.controller.RepoController
import org.littlegit.client.engine.i18n.Localizer
import tornadofx.*

abstract class BaseView(title: String? = null, private val fullScreen: Boolean = false) : View(title ?: "") {
    protected val localizer: Localizer by inject()
    protected val repoController: RepoController by inject()

    override fun onDock() {
        super.onDock()
        currentStage?.isMaximized = fullScreen
    }
}

abstract class BaseFragment(): Fragment() {
    protected val localizer: Localizer by inject()
}