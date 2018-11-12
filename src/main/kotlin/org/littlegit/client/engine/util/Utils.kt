package org.littlegit.client.engine.util

import org.littlegit.core.LittleGitCommandResult

typealias SimpleCallback<T> = (T) -> Unit

typealias LittleGitCommandCallback<T> = SimpleCallback<LittleGitCommandResult<T>>