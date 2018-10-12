package org.littlegit.client.ui.util

import org.littlegit.client.engine.controller.RepoController
import org.littlegit.client.ui.view.ChooseRepoView
import org.littlegit.client.ui.view.MainView
import tornadofx.*

object NavigationUtils {

    fun navigateFromLoginFlow(context: UIComponent, repoController: RepoController) {
        if (repoController.hasCurrentRepo) {
            repoController.getCurrentRepo {
                if (it != null) {
                    context.replaceWith(ChooseRepoView::class)
                } else {
                    context.replaceWith(MainView::class)
                }
            }
        } else {
            context.replaceWith(ChooseRepoView::class)
        }
    }
}