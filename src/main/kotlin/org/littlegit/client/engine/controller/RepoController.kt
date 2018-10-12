package org.littlegit.client.engine.controller

import org.littlegit.client.engine.db.RepoDb
import org.littlegit.client.engine.model.Repo
import tornadofx.*
import java.io.File
import java.time.LocalDateTime

class RepoController: Controller(), InitableController {


    private val repoDb: RepoDb by inject()
    private var currentRepoId: String? = null
    private var currentRepo: Repo? = null; set(newValue) {
        field = newValue
        newValue?.let { currentRepoId = it.localId }
    }

    val hasCurrentRepo: Boolean; get() = currentRepoId != null

    override fun onStart(onReady: (InitableController) -> Unit) {
        repoDb.getCurrentRepoId {
            currentRepoId = it
            onReady(this)
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

    fun setCurrentRepo(dir: File, completion: () -> Unit) {
        repoDb.getAllRepos { repos ->

            repos?.find { it.path == dir.toPath() }?.let {
                it.lastAccessedDate = LocalDateTime.now()
                currentRepo = it
                repoDb.updateRepos()
            } ?: run {
                val repo = Repo(path = dir.toPath())
                repoDb.saveRepo(repo)
                currentRepo = repo
            }

            initialiseRepoIfNeeded(currentRepo!!, completion)
        }
    }

    private fun initialiseRepoIfNeeded(repo: Repo, completion: () -> Unit) {

    }
}