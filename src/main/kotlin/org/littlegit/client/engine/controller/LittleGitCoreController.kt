package org.littlegit.client.engine.controller

import org.littlegit.core.LittleGitCore
import tornadofx.*
import java.nio.file.Path
import java.util.concurrent.Executors

class LittleGitCoreController: Controller() {

    private val executor = Executors.newSingleThreadExecutor()
    private var littleGitCore: LittleGitCore? = null
    var currentRepoPath: Path? = null; set(newValue) {
        field = newValue

        if (newValue != null) {
            littleGitCore = LittleGitCore
                    .Builder()
                    .setRepoDirectoryPath(newValue)
                    .build()
        }
    }
    // Should only be called on the main ui thread
    fun doNext(action: (LittleGitCore) -> Unit) {
        if (littleGitCore == null) {
            throw Exception("Littlegit core not initialized")
        }

        executor.execute {
            action(littleGitCore!!)
        }
    }
}