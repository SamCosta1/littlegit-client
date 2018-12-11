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
import org.littlegit.core.model.FileDiff
import org.littlegit.core.model.RawCommit
import org.littlegit.client.engine.util.LittleGitCommandCallback
import org.littlegit.client.engine.util.SimpleCallback
import org.littlegit.core.LittleGitCommandResult
import org.littlegit.core.LittleGitCore
import org.littlegit.core.commandrunner.GitResult
import org.littlegit.core.model.*
import tornadofx.*
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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
    private val localizer: Localizer by inject()
    private val networkController: NetworkController by inject()
    private val userController: UserController by inject()

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

    fun notifyRepoDirectoryMissingSync(currentRepoPath: Path?) {

        currentRepo?.let {
            currentRepo = null
            repoDb.deleteRepoSync(it)
        }
    }

    fun updateRepoIfNeeded() {
        if (!networkController.isInternetAvailable() || currentRepo == null || currentlyUpdating) {
            return
        }

        littleGitCoreController.doNextLongRunning (false) {
            if (it == null) {
                return@doNextLongRunning
            }

            it.repoModifier.fetch(true)

            littleGitCoreController.doNext { _ ->
                val currentBranch: LocalBranch? = getCurrentBranch(it)

                if (currentBranch?.upstream != null && currentBranch.commitHash != currentBranch.upstream?.commitHash) {
                    val changesToRemote = it.repoReader.getLogBetween(currentBranch, currentBranch.upstream!!)
                    if (changesToRemote.data?.isEmpty() == false) {
                        runLater { fire(UpdateAvailable) }
                    }
                }
            }
        }
    }

    private fun getCurrentBranch(it: LittleGitCore): LocalBranch? {
        return it.repoReader.getBranches().data?.find { it.isHead } as? LocalBranch?
    }

    fun updateToLatestFetched(callback: SimpleCallback<LittleGitCommandResult<MergeResult>>) {
        littleGitCoreController.doNext {
            if (it == null) {
                callback.invoke(LittleGitCommandResult.buildError(GitError.NotARepo(emptyList())))
                return@doNext
            }
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

    fun getUnifiedReposList(completion: (List<RepoDescriptor>?) -> Unit) {
        repoDb.getAllRepos { localList ->

            repoApi.getAllRepos().enqueue {
                if (it.isSuccess) {
                    completion(unifyReposList(localList, it.body))
                } else {
                    completion(localList)
                }
            }
        }
    }

    private fun unifyReposList(localList: List<Repo>?, remoteList: List<RemoteRepoSummary>?): List<RepoDescriptor> {
        val localRepos = localList ?: emptyList()

        val allRepos: MutableList<RepoDescriptor> = localRepos.toMutableList()
        val remoteRepos = remoteList ?: emptyList()

        remoteRepos.forEach { remoteRepo ->
            if (localRepos.find { remoteRepo.id == it.remoteRepo?.id } == null) {
                allRepos.add(remoteRepo)
            }
        }

        return allRepos
    }

    fun getRepos(completion: (List<Repo>?) -> Unit) {
        repoDb.getAllRepos(completion)
    }

    fun setCurrentRepo(dir: File, completion: (success: Boolean, repo: Repo) -> Unit) {
        repoDb.getAllRepos { repos ->

            val existingRepo = repos?.find { it.path == dir.toPath() }

            if (existingRepo != null) {
                existingRepo.lastAccessedDate = OffsetDateTime.now()
                currentRepo = existingRepo
                repoDb.updateRepo(existingRepo) {
                    repoDb.setCurrentRepoId(currentRepoId!!) {
                        initialiseRepoIfNeeded(currentRepo!!, completion)
                    }

                }
            } else {
                val repo = Repo(path = dir.toPath())
                repoDb.saveRepo(repo) {
                    currentRepo = repo
                    repoDb.setCurrentRepoId(currentRepoId!!) {
                        initialiseRepoIfNeeded(currentRepo!!, completion)
                    }
                }
            }

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
            it?.repoModifier?.addRemote(REMOTE_NAME, remoteRepo.cloneUrlPath)
        }
    }

    fun setCurrentRepo(repo: RepoDescriptor, completion: (success: Boolean, repo: Repo?) -> Unit) {
        when (repo) {
            is Repo -> {
                initialiseRepoIfNeeded(repo) { success, repoRes ->
                    repoDb.setCurrentRepoId(repo.localId) {
                        completion(success, repoRes)
                    }
                }
            }
            is RemoteRepoSummary -> {
                clone(repo) { _, localRepo ->
                    repoDb.saveRepo(localRepo) {
                        repoDb.setCurrentRepoId(localRepo.localId) { _ ->
                            completion(true, localRepo)
                        }
                    }
                }
            }
            else -> throw Exception("This shouldn't ever happen!")
        }
    }

    fun initialiseRepoIfNeeded(repo: Repo, completion: (success: Boolean, repo: Repo) -> Unit) {
        currentLog = emptyList()
        littleGitCoreController.currentRepoPath = repo.path
        currentRepo = repo

        littleGitCoreController.doNext { core ->
            if (core == null) {
                completion(false, repo)
                return@doNext
            }

            val isSuccess = if (core.repoReader.isInitialized().data != true) {
                !core.repoModifier.initializeRepo(bare = false).isError
            } else {
                true
            }

            userController.currentUser?.email?.let { email ->
                core.configModifier.setEmail(email)
            }

            userController.currentUser?.username?.let { username ->
                core.configModifier.setName(username)
            }

            setPrivateKeyPath(core) {
                runLater { completion(isSuccess, repo) }
            }



        }
    }

    private fun setPrivateKeyPath(core: LittleGitCore, completion: LittleGitCommandCallback<Void?>? = null) {
        val path = sshController.getPrivateKeyPath()

        if (path != null) {
            val result = core.configModifier.setSshKeyPath(path)
            completion?.invoke(result)
        }
    }

    fun push() {
        littleGitCoreController.doNext {
            if (it == null) {
                return@doNext
            }

            it.repoReader.getBranches().data?.find { it.isHead }?.let { currentBranch ->
                val result = it.repoModifier.push(REMOTE_NAME, currentBranch.branchName)
            }
        }
    }

    fun stageAllAndCommit(message: String, callback: () -> Unit) {
        littleGitCoreController.doNext {
            if (it == null) {
                callback()
                return@doNext
            }
            val unstagedChanges = stageAllChanges(it)

            if (unstagedChanges?.hasTrackedChanges == true || unstagedChanges?.unTrackedFiles?.isNotEmpty() == true) {
                commitAndPush(it, message)
            }

            runLater{ callback() }
        }
    }

    fun checkoutCommit(commit: RawCommit, callback: LittleGitCommandCallback<Branch?>) {
        littleGitCoreController.doNext {
            if (it == null) {
                return@doNext
            }

            val branches = it.repoReader.getBranches().data
            var branch = branches?.find { it.commitHash == commit.hash }

            if (branch == null) {
                val createResult = it.repoModifier.createBranch("Branch-${commit.hash}", commit)

                if (createResult.isError) {
                    callback(createResult)
                    return@doNext
                } else {
                    branch = createResult.data
                }
            }
            
            val result = it.repoModifier.checkoutBranch(branch!!, true)

            if (result.isError) {
                result.result
                callback(LittleGitCommandResult(null, result.result))
            } else {
                callback(LittleGitCommandResult(branch, result.result))
            }
        }
    }

    fun clone(remoteRepoSummary: RemoteRepoSummary, localPath: Path? = null, callback: (success: Boolean, repo: Repo) -> Unit) {
        val path = localPath ?: Paths.get(System.getProperty("user.home"), remoteRepoSummary.repoName)
        Files.createDirectories(path)

        littleGitCoreController.currentRepoPath = path
        val repo = Repo(remoteRepoSummary.id.toString(), path, remoteRepo = remoteRepoSummary)

        initialiseRepoIfNeeded(repo) { _,_ ->
            littleGitCoreController.doNext(true) {
                if (it == null) {
                    runLater { callback(false, repo) }
                    return@doNext
                }

                it.repoModifier.addRemote(REMOTE_NAME, remoteRepoSummary.cloneUrlPath)
                val fetchResult = it.repoModifier.fetch(all = true, quiet = true)

                if (fetchResult.isError) {
                    runLater { callback(false, repo) }
                    return@doNext
                }

                // If there are no commits i.e. the remote is empty, then don't try to merge anything because there's nothing to merge
                if (it.repoReader.getCommitList().data?.isEmpty() == true) {
                    callback(true, repo)
                    return@doNext
                }

                val branches = it.repoReader.getBranches().data
                val remoteMaster = branches?.find { it.branchName == "master" } ?: branches?.firstOrNull()
                if (remoteMaster == null || remoteMaster !is RemoteBranch) {
                    runLater { callback(false, repo) }
                } else {
                    val result = it.repoModifier.merge(remoteMaster)

                    // There's now only one local branch, the newly created one
                    val currentBranch = it.repoReader.getBranches().data?.find { it is LocalBranch } as LocalBranch
                    it.repoModifier.setRemoteTracking(currentBranch, remoteMaster)
                    runLater { callback(!result.isError, repo) }
                }
            }
        }
    }

    fun commitAndPush(it: LittleGitCore, message: String) {
        val result = it.repoModifier.commit(message)
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
            if (it == null) {
                return@doNext
            }

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
            if (it == null) {
                completion.invoke(LittleGitCommandResult.buildError(GitError.NotARepo(emptyList())))
                return@doNext
            }

            Files.write(file.file.toPath(), file.content, Charset.forName("UTF-8"))

            val stageResult = it.repoModifier.stageFile(file.file)
            completion(stageResult)
        }
    }

}