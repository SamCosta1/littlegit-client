package org.littlegit.client.engine.model

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

interface RepoDescriptor {

}

data class Repo(val localId: String = UUID.randomUUID().toString(), val path: Path, var lastAccessedDate: OffsetDateTime = OffsetDateTime.now(), var remoteRepo: RemoteRepoSummary? = null): RepoDescriptor {
    val existsLocally: Boolean; get() = path.toFile().exists()

    override fun equals(other: Any?): Boolean {
        return other is Repo &&
                other.lastAccessedDate.withNano(0) == lastAccessedDate.withNano(0)
        && other.localId == localId
        && other.path == path
        && other.remoteRepo == remoteRepo
    }
}

data class RemoteRepoSummary(val id: Int, val repoName: String, val createdDate: OffsetDateTime, val description: String, val cloneUrlPath: String): RepoDescriptor {
    override fun equals(other: Any?): Boolean {
        return other is RemoteRepoSummary
        && other.id == id
        && other.repoName == repoName
        && other.createdDate.withNano(0) == createdDate.withNano(0)
        && other.description == description
        && other.cloneUrlPath == cloneUrlPath
    }
}

data class CreateRepoRequest(val repoName: String, val description: String = "")