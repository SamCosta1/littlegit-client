package org.littlegit.client.engine.util

import org.littlegit.core.LittleGitCommandResult
import java.text.MessageFormat

typealias SimpleCallback<T> = (T) -> Unit

typealias LittleGitCommandCallback<T> = SimpleCallback<LittleGitCommandResult<T>>

fun String.inject(vararg params: Any) = MessageFormat.format(this, *params)!!