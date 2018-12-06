package org.littlegit.client.engine.db

import org.littlegit.client.engine.model.Repo
import org.littlegit.client.engine.util.SimpleCallback
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
            return
        }

        readListAsync(REPOS_KEY, Repo::class.java) {
            repos = it
            completion(it)
        }
    }

    fun saveRepo(repo: Repo, completion: SimpleCallback<Unit>? = null) {
        getAllRepos {
            val newList = it?.toMutableList() ?: mutableListOf()
            newList.add(repo)
            updateRepos(newList, completion)
        }
    }

    fun updateRepos(allRepos: List<Repo>? = repos, completion: SimpleCallback<Unit>? = null) {
        repos = allRepos

        if (allRepos == null) {
            completion?.invoke(Unit)
        } else {
            writeListAsync(REPOS_KEY, allRepos, Repo::class.java, completion)
        }

    }

    fun getCurrentRepoId(completion: (String?) -> Unit) {
        readAsync(CURRENT_REPO_ID, String::class.java) {
            completion(it)
        }
    }

    fun setCurrentRepoId(repoId: String, completion: SimpleCallback<Unit>? = null) {
        writeAsync(CURRENT_REPO_ID, repoId, String::class.java, completion)
    }

    fun updateRepo(repo: Repo, completion: SimpleCallback<Unit>? = null) {
        getAllRepos { repos ->
            val mutableList = repos?.toMutableList()
            val index = mutableList?.indexOfFirst { it.localId == repo.localId }

            if (index != null) {
                mutableList[index] = repo
                updateRepos(mutableList, completion)
            }
        }
    }

    fun deleteRepo(repo: Repo, completion: SimpleCallback<Unit>? = null) {

        getAllRepos { repos ->
            val mutableList = repos?.toMutableList()

            mutableList?.removeAll { it.localId == repo.localId }
            updateRepos(mutableList, completion)

        }
    }

    // Mainly for testing
    fun clearCache() {
        repos = null
    }

    fun deleteRepoSync(repo: Repo) {
        val allRepos = repos ?: readList(REPOS_KEY, Repo::class.java) ?: emptyList()
        val mutable = allRepos.toMutableList()

        mutable.removeAll { it.localId == repo.localId }
        writeList(REPOS_KEY, mutable, Repo::class.java)
        repos = mutable
    }
}

fun String.inject(vararg params: Any) = MessageFormat.format(this, *params)!!
