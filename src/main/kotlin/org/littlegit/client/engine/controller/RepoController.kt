package org.littlegit.client.engine.controller

import org.littlegit.client.engine.db.RepoDb
import org.littlegit.client.engine.model.Repo
import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.model.FileDiff
import tornadofx.*
import java.io.File
import java.time.OffsetDateTime

class RepoController: Controller(), InitableController {

    private val littleGitCoreController: LittleGitCoreController by inject()
    private val repoDb: RepoDb by inject()
    private var currentRepoId: String? = null
    private var currentRepo: Repo? = null; set(newValue) {
        field = newValue
        newValue?.let { currentRepoId = it.localId }
    }

    val hasCurrentRepo: Boolean; get() = currentRepoId != null

    override fun onStart(onReady: (InitableController) -> Unit) {
        repoDb.getCurrentRepoId { repoId ->
            repoDb.getAllRepos {
                currentRepo = it?.find { it.localId == repoId }
                littleGitCoreController.currentRepoPath = currentRepo?.path?.toAbsolutePath()
                onReady(this)

            }
        }
    }

    fun getCurrentRepo(completion: (Repo?) -> Unit) {
        if (currentRepo != null) {
            completion(currentRepo)
        } else {
            getRepos {
                completion(it?.find { it.localId == currentRepoId })
            }
        }
    }

    fun getRepos(completion: (List<Repo>?) -> Unit) {
        repoDb.getAllRepos(completion)
    }

    fun setCurrentRepo(dir: File, completion: (success: Boolean) -> Unit) {
        repoDb.getAllRepos { repos ->

            repos?.find { it.path == dir.toPath() }?.let {
                it.lastAccessedDate = OffsetDateTime.now()
                currentRepo = it
                repoDb.updateRepos()
            } ?: run {
                val repo = Repo(path = dir.toPath())
                repoDb.saveRepo(repo)
                currentRepo = repo
            }

            repoDb.setCurrentRepoId(currentRepoId!!)
            initialiseRepoIfNeeded(currentRepo!!, completion)
        }
    }

    private fun initialiseRepoIfNeeded(repo: Repo, completion: (success: Boolean) -> Unit) {
        littleGitCoreController.currentRepoPath = repo.path

        littleGitCoreController.doNext {
            if (it.repoReader.isInitialized().data == true) {
                runLater { completion(true) }
            } else {
                val result = it.repoModifier.initializeRepo(bare = false)
                runLater { completion(!result.isError) }
            }
        }
    }

    fun stageAllAndCommit() {
        littleGitCoreController.doNext {
            val unstagedChanges = it.repoReader.getUnStagedChanges().data
            unstagedChanges?.unTrackedFiles?.forEach { file ->
                val result = it.repoModifier.stageFile(file.file)
                println(result)
            }
            unstagedChanges?.trackedFilesDiff?.fileDiffs?.forEach { fileDiff ->
                val file = when(fileDiff) {
                    is FileDiff.ChangedFile -> fileDiff.filePath
                    is FileDiff.DeletedFile -> fileDiff.filePath
                    is FileDiff.RenamedFile -> fileDiff.newPath
                    is FileDiff.NewFile -> fileDiff.filePath
                    else -> ""
                }
                it.repoModifier.stageFile(File(file))
            }

            if (unstagedChanges?.hasTrackedChanges == true || unstagedChanges?.unTrackedFiles?.isNotEmpty() == true) {
                it.repoModifier.commit("Commit message")
            }
        }
    }

}