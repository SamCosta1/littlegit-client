package org.littlegit.client.ui.view

import org.littlegit.client.engine.controller.LittleGitCoreController
import org.littlegit.client.engine.controller.RepoController
import org.littlegit.client.engine.i18n.Localizer
import org.littlegit.client.ui.app.StateStore
import tornadofx.*

abstract class BaseView(title: String? = null, private val fullScreen: Boolean? = null) : View(title ?: "") {
    protected val localizer: Localizer by inject()
    protected val repoController: RepoController by inject()
    protected val littleGitCoreController: LittleGitCoreController by inject()
    protected val stateStore: StateStore by inject()

    override fun onDock() {
        super.onDock()
        if (fullScreen != null) {
            currentStage?.isMaximized = fullScreen
        }

    }
}

abstract class BaseFragment(): Fragment() {
    protected val localizer: Localizer by inject()
}