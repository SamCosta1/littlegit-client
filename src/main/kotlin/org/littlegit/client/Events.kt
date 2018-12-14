package org.littlegit.client

import org.littlegit.core.model.RawCommit
import tornadofx.*
import java.nio.file.Path

object UnauthorizedEvent: FXEvent()

object UpdateAvailable: FXEvent()

object ConflictsResolvedEvent: FXEvent()

object HideCommitView: FXEvent()

object LogoutEvent: FXEvent()

class RepoNoLongerExistsEvent(val path: Path): FXEvent()

class ShowCommitEvent(val commit: RawCommit): FXEvent()

class CreateCommitEvent(val message: String): FXEvent()