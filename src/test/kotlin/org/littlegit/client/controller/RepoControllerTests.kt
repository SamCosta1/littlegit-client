package org.littlegit.client.controller

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.littlegit.client.engine.controller.LittleGitCoreController
import org.littlegit.client.engine.controller.RepoController
import org.littlegit.client.engine.controller.SShController
import org.littlegit.client.engine.db.RepoDb
import org.littlegit.client.testUtils.RepoHelper
import org.littlegit.client.testUtils.TestCommandHelper
import org.littlegit.client.testUtils.upon
import org.littlegit.core.LittleGitCore
import org.mockito.Mockito.mock
import java.nio.file.Path
import kotlin.test.*

class RepoControllerTests: BaseControllerTest() {

    private val littleGitCoreController = LittleGitCoreController()
    private lateinit var repoController: RepoController
    private lateinit var littleGitCore: LittleGitCore
    private lateinit var commandHelper: TestCommandHelper

    private lateinit var sshController: SShController
    @Rule @JvmField var sshFolder = TemporaryFolder()
    @Rule @JvmField var repoFolder = TemporaryFolder()
    private lateinit var privateKeyPath: Path
    private lateinit var repoDb: RepoDb

    override fun setup() {
        super.setup()

        commandHelper = TestCommandHelper(repoFolder.root)
        sshController = mock(SShController::class.java)
        littleGitCore = buildCore(repoFolder.root)
        privateKeyPath = sshFolder.root.toPath().resolve("id_rsa")
        upon(sshController.getPrivateKeyPath()).thenReturn(privateKeyPath)

        addToScope(sshController, SShController::class)
        addToScope(littleGitCoreController, LittleGitCoreController::class)

        repoController = findInTestScope(RepoController::class)
        repoDb = findInTestScope(RepoDb::class)
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

    @Test
    fun testSetCurrentRepo_WithDirectory_NotAlreadyARepo_IsSuccessful() = runTest { completion ->

        repoController.setCurrentRepo(repoFolder.root) { success, repo ->
            assertTrue(success)
            assertEquals(repoFolder.root.toPath(), repo.path)
            assertTrue(littleGitCore.repoReader.isInitialized().data!!)

            repoController.getRepos { allRepos ->

                assertTrue(allRepos?.contains(repo)!!)
                assertEquals(1, allRepos.size)

                repoController.getCurrentRepo {  currentRepo ->
                    assertEquals(repo, currentRepo)
                    completion()
                }
            }
        }
    }

    @Test
    fun testSetCurrentRepo_WithDirectory_AlreadyARepo_IsSuccessful() = runTest { completion ->

        // Setup: Make the repo already exist
        littleGitCore.repoModifier.initializeRepo()

        val repo = RepoHelper.createRepo(path = repoFolder.root.toPath())
        repoDb.updateRepos(listOf(repo)) {

            repoController.setCurrentRepo(repoFolder.root) { success, foundRepo ->
                assertTrue(success)
                assertEquals(repo, foundRepo)
                assertEquals(repoFolder.root.toPath(), foundRepo.path)
                assertTrue(littleGitCore.repoReader.isInitialized().data!!)

                repoController.getRepos { allRepos ->

                    assertTrue(allRepos?.contains(foundRepo)!!)
                    assertEquals(1, allRepos.size)

                    repoController.getCurrentRepo { currentRepo ->
                        assertEquals(foundRepo, currentRepo)
                        completion()
                    }
                }
            }
        }
    }

    @Test
    fun testSetCurrentRepo_WithLocalRepo_IsSuccessful() = runTest { completion ->
        littleGitCore.repoModifier.initializeRepo()

        val repo = RepoHelper.createRepo(path = repoFolder.root.toPath())

        repoController.setCurrentRepo(repo) { success, foundRepo ->
            assertTrue(success)
            assertEquals(repo, foundRepo)
            assertEquals(repoFolder.root.toPath(), foundRepo!!.path)
            assertEquals(privateKeyPath, littleGitCore.configModifier.getSshKeyPath().data)

            repoController.getCurrentRepo { currentRepo ->
                assertEquals(foundRepo, currentRepo)
                completion()
            }
        }
    }

    @Test
    fun testSetCurrentRepo_NoLongerExists_FailsGracefully() = runTest { completion ->
        val repoFolder = repoFolder.root.toPath().resolve("NonExistentDirectory")

        val repo = RepoHelper.createRepo(path = repoFolder)
        repoDb.saveRepo(repo) {


            repoController.setCurrentRepo(repo) { success, _ ->
                assertFalse(success)

                // Check it's been removed from the list
                repoDb.getAllRepos { repos ->
                    assertFalse(repos?.contains(repo)!!)
                    completion()
                }
            }
        }
    }

    @Test
    fun testCheckoutPreviousCommit_IsSuccessful() = runTest { completion ->

        repoController.setCurrentRepo(repoFolder.root) { _, _ ->
            val testFile = "the_shire.txt"

            // Create some commits
            val commitMessage1 = "Add-the-shire"
            commandHelper.writeToFile(testFile, "Th")
                    .addAll()
                    .commit(commitMessage1)

            val commitMessage2 = "Add-mordor"
            commandHelper.writeToFile(testFile, "This")
                    .addAll()
                    .commit(commitMessage2)

            val commitMessage3 = "Add-gondor"
            commandHelper.writeToFile(testFile, "Thist")
                    .addAll()
                    .commit(commitMessage3)

            val commits = littleGitCore.repoReader.getCommitList()
            assertEquals(3, commits.data?.size)

            val headCommit = commits.data?.find { it.isHead }
            assertEquals(commitMessage3, headCommit?.commitSubject)

            // Now checkout back to the first commit
            val targetCommit = commits.data?.find { it.commitSubject == commitMessage1 }
            assertNotNull(targetCommit); targetCommit!!

            repoController.checkoutCommit(targetCommit) {
                val updatedCommits = littleGitCore.repoReader.getCommitList()
                val newHead = updatedCommits.data?.find { it.isHead }

                assertEquals(newHead?.hash, targetCommit.hash)
                assertNotEquals(headCommit?.hash, newHead?.hash)
                completion()
            }
        }
    }
}