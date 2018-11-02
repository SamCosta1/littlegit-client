package org.littlegit.client.engine.db

import java.nio.file.Path

class SShDb: LocalDb() {

    private var sshKeyPath: Path? = null
    companion object {
        private const val DB_KEY = "ssh_key_path"
    }

    fun setSshKeyPath(path: Path) {
        sshKeyPath = path
        writeAsync(DB_KEY, path, Path::class.java)
    }

    fun getSshKeyPath(completion: (Path?) -> Unit) {
        readAsync(DB_KEY, Path::class.java) {
            sshKeyPath = it
            completion(it)
        }
    }
}