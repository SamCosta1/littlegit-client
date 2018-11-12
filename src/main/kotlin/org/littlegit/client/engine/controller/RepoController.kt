package org.littlegit.client.engine.controller

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import org.littlegit.client.UpdateAvailable
import org.littlegit.client.engine.api.ApiCallCompletion
import org.littlegit.client.engine.api.CallFailure
import org.littlegit.client.engine.api.RepoApi
import org.littlegit.client.engine.api.enqueue
import org.littlegit.client.engine.db.RepoDb
import org.littlegit.client.engine.i18n.Localizer
import org.littlegit.client.engine.model.*
import org.littlegit.client.engine.util.LittleGitCommandCallback
import org.littlegit.client.engine.util.SimpleCallback
import org.littlegit.core.LittleGitCommandResult
import org.littlegit.core.LittleGitCore
import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.model.*
import tornadofx.*
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.time.OffsetDateTime
import java.util.*
import kotlin.concurrent.schedule

class RepoController: Controller(), InitableController {

    companion object {
        private const val REMOTE_NAME = "littlegit-origin"
    }

    private val littleGitCoreController: LittleGitCoreController by inject()
    private val sshController: SShController by inject()
    private val apiController: ApiController by inject()
    private val repoApi: RepoApi; get() = apiController.repoApi
    private val networkController: NetworkController by inject()
    private val localizer: Localizer by inject()

    private val timer = Timer()
    private val repoDb: RepoDb by inject()
    private var currentRepoId: String? = null

    // Gets set to true by UI when it is in the process of updating the local to match the remote, used to stop the controller from notifying it that it needs to happen
    var currentlyUpdating: Boolean = false;

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
        timer.schedule(300, 2000) {
            updateRepoIfNeeded()
        }
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

        networkController.networkAvailability.addListener(tornadofx.ChangeListener { _, oldValue, hasInternetAccess ->
           if (hasInternetAccess) {
               if (currentRepo != null) {
                   updateRepoIfNeeded()
               }
           }
        })
    }

    private fun updateRepoIfNeeded() {
        if (!networkController.isInternetAvailable() || currentRepo == null || currentlyUpdating) {
            return
        }

        littleGitCoreController.doNext {
            it.repoModifier.fetch(true)

            val currentBranch = getCurrentBranch(it)

            if (currentBranch?.upstream != null) {
                val changesToRemote = it.repoReader.getLogBetween(currentBranch, currentBranch.upstream!!)
                if (changesToRemote.data?.isEmpty() == false) {
                    fire(UpdateAvailable)
                }
            }
        }
    }

    private fun getCurrentBranch(it: LittleGitCore): LocalBranch? {
        return it.repoReader.getBranches().data?.find { it.isHead } as? LocalBranch?
    }

    fun updateToLatestFetched(callback: SimpleCallback<LittleGitCommandResult<MergeResult>>) {
        littleGitCoreController.doNext {
            // First commit any unstaged changes since otherwise we'll be in a world of trouble

            val unstagedChanges = stageAllChanges(it)
            if (unstagedChanges?.hasTrackedChanges == true || unstagedChanges?.unTrackedFiles?.isNotEmpty() == true) {
                it.repoModifier.commit(localizer[I18nKey.AutoCommitMessage])
            }

            getCurrentBranch(it)?.upstream?.let { upstream ->

                val mergeResult = it.repoModifier.merge(upstream)
                runLater { callback(mergeResult) }
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

    fun setCurrentRepo(dir: File, completion: (success: Boolean, repo: Repo) -> Unit) {
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

    fun createRemoteRepo(repo: Repo, completion: ApiCallCompletion<RemoteRepoSummary>) {
        createRemoteRepo(repo, repo.path.fileName.toString(), 0, completion)
    }

    // Can't guarantee the user won't have multiple directories of the same name, though that'd be stupid. So if this happens just do the same as people
    // Are used to when duplicating files. e.g.  myRepo -> myRepo-1
    private fun createRemoteRepo(repo: Repo, repoName: String, iteration: Int, completion: ApiCallCompletion<RemoteRepoSummary>) {
        val name = if (iteration > 0) {
            "$repoName-$iteration"
        } else {
            repoName
        }

        repoApi.createRemoteRepo(CreateRepoRequest(name)).enqueue {
            if (it.isSuccess && it.body != null) {
                repo.remoteRepo = it.body
                repoDb.updateRepo(repo)
                setOrigin(remoteRepo = it.body)
                completion(it)
                return@enqueue
            }

            if (it.errorBody is CallFailure.ApiError && it.errorBody.localisedMessage.contains(I18nKey.ValueAlreadyExists)) {
                createRemoteRepo(repo, repoName, iteration + 1, completion)
            } else {
                completion(it)
            }
        }
    }

    private fun setOrigin(remoteRepo: RemoteRepoSummary) {
        littleGitCoreController.doNext {
            it.repoModifier.addRemote(REMOTE_NAME, remoteRepo.cloneUrlPath)
        }
    }

    fun setCurrentRepo(repo: Repo, completion: (success: Boolean, repo: Repo) -> Unit) {
        repoDb.setCurrentRepoId(repo.localId)
        initialiseRepoIfNeeded(repo, completion)
    }

    private fun initialiseRepoIfNeeded(repo: Repo, completion: (success: Boolean, repo: Repo) -> Unit) {
        currentLog = emptyList()
        littleGitCoreController.currentRepoPath = repo.path
        currentRepo = repo

        littleGitCoreController.doNext { core ->

            sshController.getPrivateKeyPath {
                it?.let { core.configModifier.setSshKeyPath(it) }
            }

            if (core.repoReader.isInitialized().data == true) {
                runLater { completion(true, repo) }
                loadLog()
            } else {
                val result = core.repoModifier.initializeRepo(bare = false)
                runLater { completion(!result.isError, repo) }
            }
        }
    }

    fun push() {
        littleGitCoreController.doNext {
            it.repoReader.getBranches().data?.find { it.isHead }?.let { currentBranch ->
                val result = it.repoModifier.push(REMOTE_NAME, currentBranch.branchName)
                println(result)
            }
        }
    }

    fun stageAllAndCommit(callback: () -> Unit) {
        littleGitCoreController.doNext {
            val unstagedChanges = stageAllChanges(it)

            if (unstagedChanges?.hasTrackedChanges == true || unstagedChanges?.unTrackedFiles?.isNotEmpty() == true) {
                commitAndPush(it)
            }

            runLater{ callback() }
        }
    }

    fun commitAndPush(it: LittleGitCore) {
        val result = it.repoModifier.commit("Commit message")
        if (!result.isError) {
            push()
        }
    }

    private fun stageAllChanges(it: LittleGitCore): UnstagedChanges? {
        val unstagedChanges = it.repoReader.getUnStagedChanges().data
        unstagedChanges?.unTrackedFiles?.forEach { file ->
            val result = it.repoModifier.stageFile(file.file)
            println(result)
        }
        unstagedChanges?.trackedFilesDiff?.fileDiffs?.forEach { fileDiff ->
            when (fileDiff) {
                is FileDiff.ChangedFile -> fileDiff.filePath
                is FileDiff.DeletedFile -> fileDiff.filePath
                is FileDiff.RenamedFile -> fileDiff.newPath
                is FileDiff.NewFile -> fileDiff.filePath
                else -> null
            }?.let { path ->
                it.repoModifier.stageFile(path.toFile())
            }
        }
        return unstagedChanges
    }

    fun loadLog() {
        littleGitCoreController.doNext(notifyListeners = false) {
            val commits = it.repoReader.getCommitList().data ?: emptyList()
            runLater {
                currentLog = commits
            }
        }
    }

    fun writeAndStage(file: LittleGitFile?, completion: LittleGitCommandCallback<Unit>) {
        if (file == null) {
            completion(LittleGitCommandResult(Unit, GitResult.Success(emptyList())))
            return
        }

        littleGitCoreController.doNext {
            Files.write(file.file.toPath(), file.content, Charset.forName("UTF-8"))

            val stageResult = it.repoModifier.stageFile(file.file)
            completion(stageResult)
        }
    }

}