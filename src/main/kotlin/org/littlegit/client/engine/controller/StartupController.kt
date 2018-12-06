package org.littlegit.client.engine.controller

import org.littlegit.client.engine.i18n.Localizer
import tornadofx.*
import java.util.concurrent.atomic.AtomicInteger

class StartupController: Controller() {

    private val controllers: List<InitableController> = listOf(
            find(Localizer::class),
            find(AuthController::class),
            find(UserController::class),
            find(RepoController::class)
    )

    private var numFinished = AtomicInteger(0)

    fun onStartup(onSetupComplete: () -> Unit) {

        // Find it so it starts watching the network
        find(NetworkController::class)
        find(GitController::class)

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
