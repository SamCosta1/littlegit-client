package org.littlegit.client

import org.littlegit.core.model.RawCommit
import tornadofx.*

object UnauthorizedEvent: FXEvent()

object UpdateAvailable: FXEvent()

object ConflictsResolvedEvent: FXEvent()

object HideCommitView: FXEvent()

class ShowCommitEvent(val commit: RawCommit): FXEvent()

class CreateCommitEvent(val message: String): FXEvent()