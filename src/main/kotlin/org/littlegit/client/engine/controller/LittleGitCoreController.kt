package org.littlegit.client.engine.controller

import org.littlegit.client.engine.model.RemoteRepoSummary
import org.littlegit.client.engine.util.SimpleCallback
import org.littlegit.core.LittleGitCore
import tornadofx.*
import java.nio.file.Path
import java.util.concurrent.Executors

class LittleGitCoreController: Controller() {

    private val listeners: MutableList<() -> Unit> = mutableListOf()


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

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    // Should only be called on the main ui thread
    fun doNext(notifyListeners: Boolean = true, action: (LittleGitCore) -> Unit) {
        if (littleGitCore == null) {
            throw Exception("Littlegit core not initialized")
        }

        executor.execute {
            action(littleGitCore!!)

            if (notifyListeners) {
                listeners.forEach { it.invoke() }
            }
        }
    }
}