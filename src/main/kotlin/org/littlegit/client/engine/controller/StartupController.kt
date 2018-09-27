package org.littlegit.client.engine.controller

import org.littlegit.client.engine.i18n.Localizer
import tornadofx.*
import java.util.concurrent.atomic.AtomicInteger

class StartupController: Controller() {

    private val controllers: List<InitableController> = listOf(
            find(Localizer::class),
            find(AuthController::class),
            find(UserController::class)
    )

    private var numFinished = AtomicInteger(0)

    fun onStartup(onSetupComplete: () -> Unit) {
        controllers.forEach {

            it.onStart{

                val isDone = numFinished.addAndGet(1) == controllers.size

                if (isDone) {
                    onSetupComplete.invoke()
                }
            }
        }
    }

}
