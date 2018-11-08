package org.littlegit.client.testUtils

import org.littlegit.client.engine.model.RemoteRepoSummary
import org.littlegit.client.engine.model.Repo
import java.nio.file.Paths
import java.time.OffsetDateTime

object RepoHelper {
    fun createRepo(name: String = "winterfeld-plans", id: Int = 10) = Repo(localId = id.toString(), path = Paths.get("/path/to/repo"), remoteRepo = RemoteRepoSummary(id, name, OffsetDateTime.now(), "description", "cloneUrl"))
}