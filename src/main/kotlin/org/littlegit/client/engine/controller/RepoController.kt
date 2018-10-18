package org.littlegit.client.engine.controller

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import org.littlegit.client.engine.db.RepoDb
import org.littlegit.client.engine.model.Repo
import org.littlegit.core.model.FileDiff
import org.littlegit.core.model.RawCommit
import tornadofx.*
import java.io.File
import java.time.OffsetDateTime

class RepoController: Controller(), InitableController {

    private val littleGitCoreController: LittleGitCoreController by inject()
    private val repoDb: RepoDb by inject()
    private var currentRepoId: String? = null
    private var currentRepo: Repo? = null; set(newValue) {
        field = newValue
        newValue?.let {
            currentRepoId = it.localId
            currentRepoNameObservable.value = newValue.remoteRepo?.repoName ?: newValue.path.fileName.toString()
        }
    }

    val hasCurrentRepo: Boolean; get() = currentRepoId != null
    val currentRepoNameObservable: SimpleStringProperty = SimpleStringProperty(currentRepo?.path?.fileName.toString())

    var currentLog: List<RawCommit> = mutableListOf(); private set(value) {
        field = value
        logObservable.setAll(value)
    }
    val logObservable: ObservableList<RawCommit> = mutableListOf<RawCommit>().observable()

    init {
        littleGitCoreController.addListener(this::onCommandFinished)
    }

    private fun onCommandFinished() {
        loadLog()
    }

    override fun onStart(onReady: (InitableController) -> Unit) {
        repoDb.getCurrentRepoId { repoId ->
            repoDb.getAllRepos {
                currentRepo = it?.find { it.localId == repoId }
                currentRepo?.let {
                    littleGitCoreController.currentRepoPath = currentRepo?.path?.toAbsolutePath()
                    loadLog()
                }
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

    fun setCurrentRepo(repo: Repo, completion: (success: Boolean) -> Unit) {
        repoDb.setCurrentRepoId(repo.localId)
        initialiseRepoIfNeeded(repo, completion)
    }

    private fun initialiseRepoIfNeeded(repo: Repo, completion: (success: Boolean) -> Unit) {
        currentLog = emptyList()
        littleGitCoreController.currentRepoPath = repo.path
        currentRepo = repo

        littleGitCoreController.doNext {
            if (it.repoReader.isInitialized().data == true) {
                runLater { completion(true) }
                loadLog()
            } else {
                val result = it.repoModifier.initializeRepo(bare = false)
                runLater { completion(!result.isError) }
            }
        }
    }

    fun stageAllAndCommit(callback: () -> Unit) {
        littleGitCoreController.doNext {
            val unstagedChanges = it.repoReader.getUnStagedChanges().data
            unstagedChanges?.unTrackedFiles?.forEach { file ->
                val result = it.repoModifier.stageFile(file.file)
                println(result)
            }
            unstagedChanges?.trackedFilesDiff?.fileDiffs?.forEach { fileDiff ->
                when(fileDiff) {
                    is FileDiff.ChangedFile -> fileDiff.filePath
                    is FileDiff.DeletedFile -> fileDiff.filePath
                    is FileDiff.RenamedFile -> fileDiff.newPath
                    is FileDiff.NewFile -> fileDiff.filePath
                    else -> null
                } ?.let { path ->
                    it.repoModifier.stageFile(path.toFile())
                }
            }

            if (unstagedChanges?.hasTrackedChanges == true || unstagedChanges?.unTrackedFiles?.isNotEmpty() == true) {
                it.repoModifier.commit("Commit message")
            }

            runLater{ callback() }
        }
    }

    fun loadLog() {
        littleGitCoreController.doNext(notifyListeners = false) {
            val commits = it.repoReader.getCommitList().data ?: emptyList()
            runLater {
                currentLog = commits
            }
        }
    }

}