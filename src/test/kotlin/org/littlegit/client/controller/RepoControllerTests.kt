package org.littlegit.client.controller

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.littlegit.client.engine.controller.LittleGitCoreController
import org.littlegit.client.engine.controller.RepoController
import org.littlegit.client.engine.controller.SShController
import org.littlegit.client.testUtils.RepoHelper
import org.littlegit.client.testUtils.upon
import org.littlegit.core.LittleGitCore
import org.mockito.Mockito.mock
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RepoControllerTests: BaseControllerTest() {

    private val littleGitCoreController = LittleGitCoreController()
    private lateinit var repoController: RepoController
    private lateinit var littleGitCore: LittleGitCore

    private lateinit var sshController: SShController
    @Rule @JvmField var sshFolder = TemporaryFolder()
    @Rule @JvmField var repoFolder = TemporaryFolder()
    private lateinit var privateKeyPath: Path

    override fun setup() {
        super.setup()

        sshController = mock(SShController::class.java)
        littleGitCore = buildCore(repoFolder.root)
        privateKeyPath = sshFolder.root.toPath().resolve("id_rsa")
        upon(sshController.getPrivateKeyPath()).thenReturn(privateKeyPath)

        addToScope(sshController, SShController::class)
        addToScope(littleGitCoreController, LittleGitCoreController::class)
        repoController = findInTestScope(RepoController::class)
    }

    @Test
    fun testInitializeRepo_RepoNotAlreadyInitialised_IsSuccessful() = runTest { completion ->
        val repo = RepoHelper.createRepo(path = repoFolder.root.toPath())

        repoController.initialiseRepoIfNeeded(repo) { success, _ ->
            assertTrue(success)
            assertNotNull(littleGitCoreController.currentRepoPath)

            assertEquals(privateKeyPath, littleGitCore.configModifier.getSshKeyPath().data)
            assertTrue(littleGitCore.repoReader.isInitialized().data!!)
            completion()
        }
    }

    @Test
    fun testInitializeRepo_RepoAlreadyInitialised_IsSuccessful() = runTest { completion ->
        val repo = RepoHelper.createRepo(path = repoFolder.root.toPath())
        littleGitCore.repoModifier.initializeRepo()
        
        repoController.initialiseRepoIfNeeded(repo) { success, _ ->
            assertTrue(success)
            assertNotNull(littleGitCoreController.currentRepoPath)

            assertEquals(privateKeyPath, littleGitCore.configModifier.getSshKeyPath().data)
            assertTrue(littleGitCore.repoReader.isInitialized().data!!)
            completion()
        }
    }
}