package org.littlegit.client.engine.model

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

data class Repo(val localId: String = UUID.randomUUID().toString(), val path: Path, var lastAccessedDate: OffsetDateTime = OffsetDateTime.now(), var remoteRepo: RemoteRepoSummary? = null)

data class RemoteRepoSummary(val id: Int, val repoName: String, val createdDate: OffsetDateTime, val description: String, val cloneUrlPath: String)

data class CreateRepoRequest(val repoName: String, val description: String = "")