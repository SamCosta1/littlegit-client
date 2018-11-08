package org.littlegit.client.engine.controller

import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import org.littlegit.client.engine.api.ApiCallCompletion
import org.littlegit.client.engine.api.enqueue
import org.littlegit.client.engine.db.SShDb
import org.littlegit.client.engine.model.SshKeyRequest
import org.littlegit.client.engine.util.SimpleCallback
import org.littlegit.core.util.OSType
import org.littlegit.core.util.OperatingSystemUtils
import org.littlegit.core.util.joinWithSpace
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.*


class SShController: Controller() {

    private val sshDb: SShDb by inject()
    private val userApi = find(ApiController::class.java).userApi
    private val userController: UserController by inject()

    fun checkSshKeysExist(completion: SimpleCallback<Boolean>) {
        sshDb.getSshKeyPath {  keyPath ->
            completion(keyPath?.toFile()?.exists() == true)
        }
    }

    fun generateAndAddSshKey(path: Path? = null, completion: ApiCallCompletion<Void>) {

        runAsync {

            val sshPath = when {
                path != null -> path
                defaultSshKeysExist() -> Paths.get(System.getProperty("user.dir"), ".ssh")
                else -> Paths.get(System.getProperty("user.home"), ".ssh")
            }


            generateAndWrite(sshPath)

            val publicKeyString = getPublicKeyPath(sshPath).readLines().joinWithSpace()
            userApi.addSshKey(SshKeyRequest(publicKeyString, userController.currentUser?.id!!)).enqueue {
                sshDb.setSshKeyPath(sshPath)
                completion(it)
            }
        }
    }

    private fun generateAndWrite(sshPath: Path) {
        val privatePath = getPrivateKeyPath(sshPath)
        val publicKey = getPublicKeyPath((sshPath))
        val jsch = JSch()

        Files.createDirectories(sshPath)
        val kpair = KeyPair.genKeyPair(jsch, KeyPair.RSA)

        kpair.writePrivateKey(privatePath.canonicalPath)
        kpair.writePublicKey(publicKey.canonicalPath, "SSHCerts")
        kpair.dispose() 

        if (OperatingSystemUtils.osType != OSType.Windows) {
            Files.setPosixFilePermissions(privatePath.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        }
    }

    private fun getPublicKeyPath(sshPath: Path) = sshPath.resolve("id_rsa.pub").normalize().toFile()
    private fun getPrivateKeyPath(sshPath: Path) = sshPath.resolve("id_rsa").normalize().toFile()


    // Check if the user already uses the default ssh keys because if they do, we'll leave them alone and put ours elsewhere
    // However, this will mess up if they're using an old version of git since the command to specify ssh key locations via the
    // config doesn't exist, but there's not much we can do about that
    private fun defaultSshKeysExist(): Boolean {
        return Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa").toFile().exists()
    }
}