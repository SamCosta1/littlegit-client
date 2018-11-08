package org.littlegit.client.engine.db

import org.littlegit.client.engine.util.SimpleCallback
import java.nio.file.Path

open class SShDb: LocalDb() {

    private var sshKeyPath: Path? = null
    companion object {
        private const val DB_KEY = "ssh_key_path"
    }

    open fun setSshKeyPath(path: Path, completion: SimpleCallback<Unit>? = null) {
        sshKeyPath = path
        writeAsync(DB_KEY, path, Path::class.java, completion)
    }

    open fun getSshKeyPath(completion: (Path?) -> Unit) {
        if (sshKeyPath != null) {
            completion(sshKeyPath)
            return
        }

        readAsync(DB_KEY, Path::class.java) {
            sshKeyPath = it
            completion(it)
        }
    }

    fun clearCache() {
        sshKeyPath = null
    }
}