package org.littlegit.client.engine.controller

import org.littlegit.client.RepoNoLongerExistsEvent
import org.littlegit.core.LittleGitCore
import tornadofx.*
import java.nio.file.Path
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LittleGitCoreController: Controller() {

    interface LittleGitCoreControllerListener {
        fun onCommandCompleted(){}
        fun onRepoDirectoryMissing(currentRepoPath: Path?) {}
    }

    private val listeners: MutableList<LittleGitCoreControllerListener> = mutableListOf()

    private val executor = Executors.newSingleThreadExecutor()
    private val longRunningExecutor = Executors.newSingleThreadExecutor()

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

    /**
     * Some tasks like fetch can be very long running. These are to be done on this executor so not to slow down the main one
     * Only operations which do not have an effect on anything else should be run on this thread e.g. fetching
     */
    fun doNextLongRunning(notifyListeners: Boolean = true, action: (LittleGitCore?) -> Unit) {

        execute(longRunningExecutor, action, notifyListeners)
    }

    // Should only be called on the main ui thread
    fun doNext(notifyListeners: Boolean = true, action: (LittleGitCore?) -> Unit) {
        execute(executor, action, notifyListeners)
    }

    private fun execute(executor: Executor, action: (LittleGitCore?) -> Unit, notifyListeners: Boolean) {
        if (littleGitCore == null) {
            throw Exception("Littlegit core not initialized")
        }
        executor.execute {

            val repoExists = currentRepoPath!!.toFile().exists()
            if (!repoExists) {
                listeners.forEach { it.onRepoDirectoryMissing(currentRepoPath) }
                runLater { fire(RepoNoLongerExistsEvent(currentRepoPath!!)) }
            }

            action(if (repoExists) littleGitCore!! else null)

            if (notifyListeners) {
                listeners.forEach { it.onCommandCompleted() }
            }
        }
    }
}