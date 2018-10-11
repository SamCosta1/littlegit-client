package org.littlegit.client.ui.app

import javafx.stage.Stage
import org.littlegit.client.ui.view.startup.SplashView
import tornadofx.*

class Main: App(SplashView::class, Styles::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.width = 400.0
        stage.height = 700.0

    }
}
