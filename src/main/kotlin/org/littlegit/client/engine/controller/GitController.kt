package org.littlegit.client.engine.controller

import tornadofx.*
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.schedule

class GitController: Controller(), LittleGitCoreController.LittleGitCoreControllerListener {

    private val timer = Timer()
    private val repoController: RepoController by inject()
    private val littleGitCoreController: LittleGitCoreController by inject()

    private val networkController: NetworkController by inject()

    init {
        littleGitCoreController.addListener(this)
        timer.schedule(300, 4000) {
            repoController.updateRepoIfNeeded()
        }

        networkController.networkAvailability.addListener(tornadofx.ChangeListener { _, oldValue, hasInternetAccess ->
            if (hasInternetAccess) {
                repoController.updateRepoIfNeeded()
            }
        })
    }

    override fun onCommandCompleted() {
        super.onCommandCompleted()
        repoController.loadLog()
    }


    // THIS IS SYNCHRONOUS SHOULDN'T BE CALLED ON MAIN THREAD
    override fun onRepoDirectoryMissing(currentRepoPath: Path?) {
        repoController.notifyRepoDirectoryMissingSync(currentRepoPath)
    }
}