package org.littlegit.client.db

import org.junit.Ignore
import org.junit.Test
import org.littlegit.client.engine.db.RepoDb
import org.littlegit.client.engine.model.RemoteRepoSummary
import org.littlegit.client.engine.model.Repo
import org.littlegit.client.testUtils.RepoHelper
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepoDbTests: BaseDbTests<RepoDb>(RepoDb::class) {

    @Ignore
    @Test
    fun testGetSetCurrentId_IsSuccessful() = runTest { completion ->
        val repoId = "my-repo-id"
        val updatedRepoId = "$repoId-updated"

        db.setCurrentRepoId(repoId) {

            db.getCurrentRepoId { retrieved ->
                assertEquals(repoId, retrieved)

                db.setCurrentRepoId(updatedRepoId) {

                    db.getCurrentRepoId {  updated ->
                        assertEquals(updatedRepoId, updated)

                        completion()
                    }
                }
            }
        }
    }

    @Ignore
    @Test
    fun testSaveRepo_IsSuccessful() = runTest { completion ->
        val repo1 = RepoHelper.createRepo("name1", 1)
        val repo2 = RepoHelper.createRepo("name2", 2)

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

                        completion()
                    }
                }
            }
        }
    }

    @Ignore
    @Test
    fun testUpdateRepos_IsSuccessful() = runTest { completion ->
        val list1 = listOf(RepoHelper.createRepo("name1", 1))
        val list2 = listOf(RepoHelper.createRepo("name1", 1), RepoHelper.createRepo("name2", 2), RepoHelper.createRepo("name3", 3))

        db.updateRepos(list1) {

            db.getAllRepos { result1 ->
                assertEquals(list1, result1)

                db.updateRepos(list2) {
                    db.getAllRepos { result2 ->
                        assertEquals(list2, result2)

                        completion()
                    }
                }
            }
        }
    }
}