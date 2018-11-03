package org.littlegit.client.db

import org.junit.Test
import org.littlegit.client.engine.db.RepoDb
import org.littlegit.client.engine.model.RemoteRepoSummary
import org.littlegit.client.engine.model.Repo
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepoDbTests: BaseDbTests<RepoDb>(RepoDb::class) {

    @Test
    fun testGetSetCurrentId_IsSuccessful() = runTest {
        val repoId = "my-repo-id"
        val updatedRepoId = "$repoId-updated"

        db.setCurrentRepoId(repoId) {

            db.getCurrentRepoId { retrieved ->
                assertEquals(repoId, retrieved)

                db.setCurrentRepoId(updatedRepoId) {

                    db.getCurrentRepoId {  updated ->
                        assertEquals(updatedRepoId, updated)

                        it()
                    }
                }
            }
        }
    }

    @Test
    fun testSaveRepo_IsSuccessful() = runTest {
        val repo1 = Repo(path = testFolder.root.toPath(), remoteRepo = RemoteRepoSummary(1, "name", OffsetDateTime.now(), "description", "cloneUrl"))
        val repo2 = Repo(path = testFolder.root.toPath().resolve("subpath"), remoteRepo = RemoteRepoSummary(2, "name", OffsetDateTime.now(), "description", "cloneUrl"))

        db.saveRepo(repo1) {
            db.getAllRepos { list1 ->
                assertEquals(1, list1?.size)
                assertEquals(repo1, list1?.first())

                // Save another repo
                db.saveRepo(repo2) {

                    db.getAllRepos { list2 ->
                        assertEquals(2, list2?.size)

                        assertTrue(list2?.contains(repo1) == true)
                        assertTrue(list2?.contains(repo2) == true)

                        it()
                    }
                }
            }
        }
    }

}