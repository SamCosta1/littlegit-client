package org.littlegit.client.controller

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.littlegit.client.engine.api.AuthApi
import org.littlegit.client.engine.api.UserApi
import org.littlegit.client.engine.controller.ApiController
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.controller.SShController
import org.littlegit.client.engine.controller.UserController
import org.littlegit.client.engine.db.AuthDb
import org.littlegit.client.engine.db.SShDb
import org.littlegit.client.engine.model.*
import org.littlegit.client.testUtils.UserHelper
import org.littlegit.client.testUtils.anyOf
import org.littlegit.client.testUtils.upon
import org.mockito.Mockito
import org.mockito.Mockito.mock
import retrofit2.Call
import tornadofx.*

class SshControllerTests: BaseControllerTest() {

    private lateinit var authController: AuthController
    private lateinit var userApi: UserApi
    private lateinit var sshDb: SShDb
    private lateinit var signupCall: Call<*>
    private lateinit var apiController: ApiController
    @Rule
    @JvmField var testFolder = TemporaryFolder()

    override fun setup() {
        super.setup()
        val currentUser = UserHelper.createUser()

        val userController = mock(UserController::class.java)
        apiController = mock(ApiController::class.java)
        userApi = mock(UserApi::class.java)
        sshDb = mock(SShDb::class.java)
        signupCall = mock(Call::class.java)

        upon(apiController.userApi).thenReturn(userApi)
        upon(userController.currentUser).thenReturn(currentUser)

        addToScope(apiController, ApiController::class)
        addToScope(userController, UserController::class)
        addToScope(sshDb, SShDb::class)
        authController = findInTestScope(AuthController::class)

        upon(userApi.addSshKey(anyOf(SshKeyRequest::class))).thenReturn(signupCall as Call<Void>)
    }

    @Test
    fun testGenerateSshKeys_IsSuccessful() = runTest { completion ->
        val sshController = SShController()

        sshController.generateAndAddSshKey(testFolder.root.toPath()) { response ->
            completion()
        }
    }
}