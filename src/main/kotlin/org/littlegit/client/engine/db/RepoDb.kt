package org.littlegit.client.engine.db

import org.littlegit.client.engine.model.Repo
import java.text.MessageFormat

class RepoDb: LocalDb() {
    companion object {
        private const val REPOS_KEY = "repos_key"
        private const val CURRENT_REPO_ID = "current_repos_key"
    }

    private var repos: List<Repo>? = null

    fun getAllRepos(completion: (List<Repo>?) -> Unit) {
        if (repos != null) {
            completion(repos)
        }
        readListAsync(REPOS_KEY, Repo::class.java) {
            repos = it
            completion(it)
        }
    }

    fun saveRepo(repo: Repo) {
        if (repos == null) {
            getAllRepos {
                val newList = it?.toMutableList() ?: mutableListOf()
                newList.add(repo)
                updateRepos(newList)
            }
        } else {
            val newList = repos?.toMutableList() ?: mutableListOf()
            newList.add(repo)
            updateRepos(newList)
        }
    }

    fun updateRepos(allRepos: List<Repo>? = repos) {
        allRepos?.let {
            repos = allRepos
            writeListAsync(REPOS_KEY, allRepos, Repo::class.java)
        }
    }

    fun getCurrentRepoId(completion: (String?) -> Unit) {
        readAsync(CURRENT_REPO_ID, String::class.java) {
            completion(it)
        }
    }

    fun setCurrentRepoId(repoId: String) {
        writeAsync(CURRENT_REPO_ID, repoId, String::class.java)
    }

    fun updateRepo(repo: Repo) {
        getAllRepos { repos ->
            val mutableList = repos?.toMutableList()
            val index = mutableList?.indexOfFirst { it.localId == repo.localId }

            if (index != null) {
                mutableList[index] = repo
                updateRepos(mutableList)
            }
        }
    }
}

fun String.inject(vararg params: Any) = MessageFormat.format(this, *params)!!
