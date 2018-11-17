package org.littlegit.client

import tornadofx.*

object UnauthorizedEvent: FXEvent()

object UpdateAvailable: FXEvent()

object ConflictsResolvedEvent: FXEvent()

class CreateCommitEvent(val message: String): FXEvent()