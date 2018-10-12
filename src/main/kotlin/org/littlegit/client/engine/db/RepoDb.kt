package org.littlegit.client.engine.db

import org.littlegit.client.engine.model.Repo
import java.text.MessageFormat

class RepoDb: LocalDb() {
    companion object {
        private const val REPOS_KEY = "repos_key"
        private const val CURRENT_REPO_ID = "repos_key"
    }

    private var repos: List<Repo>? = null

    fun getAllRepos(completion: (List<Repo>?) -> Unit) {
        readListAsync(REPOS_KEY, Repo::class.java) {
            repos = it
            completion(it)
        }
    }

    fun saveRepo(repo: Repo) {
        if (repos == null) {
            getAllRepos {
                val newList = it?.toMutableList()
                newList?.add(repo)
                updateRepos(newList)
            }
        } else {
            val newList = repos?.toMutableList()
            newList?.add(repo)
            updateRepos(repos)
        }
    }

    private fun updateRepos(allRepos: List<Repo>?) {
        allRepos?.let {
            writeListAsync(REPOS_KEY, allRepos, Repo::class.java)
        }
    }

    fun getCurrentRepoId(completion: (String?) -> Unit) {
        readAsync(CURRENT_REPO_ID, String::class.java) {
            completion(it)
        }
    }
}

fun String.inject(vararg params: Any) = MessageFormat.format(this, *params)!!
