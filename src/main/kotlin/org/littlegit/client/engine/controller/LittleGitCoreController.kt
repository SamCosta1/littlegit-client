package org.littlegit.client.engine.controller

import org.littlegit.core.LittleGitCore
import tornadofx.*
import java.nio.file.Path
import java.util.concurrent.Executors

class LittleGitCoreController: Controller() {

    interface LittleGitCoreControllerListener {
        fun onCommandCompleted(){}
        fun onRepoDirectoryMissing(currentRepoPath: Path?) {}
    }

    private val listeners: MutableList<LittleGitCoreControllerListener> = mutableListOf()

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

    fun addListener(listener: LittleGitCoreControllerListener) {
        listeners.add(listener)
    }

    // Should only be called on the main ui thread
    fun doNext(notifyListeners: Boolean = true, action: (LittleGitCore?) -> Unit) {
        if (littleGitCore == null) {
            throw Exception("Littlegit core not initialized")
        }

        executor.execute {

            val repoExists = currentRepoPath!!.toFile().exists()
            if (!repoExists) {
                listeners.forEach { it.onRepoDirectoryMissing(currentRepoPath) }
            }

            action(if (repoExists) littleGitCore!! else null)

            if (notifyListeners) {
                listeners.forEach { it.onCommandCompleted() }
            }
        }
    }
}