package org.littlegit.client.controller

import org.junit.Test
import org.littlegit.client.engine.api.AuthApi
import org.littlegit.client.engine.controller.ApiController
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.LoginRequest
import org.littlegit.client.engine.model.LoginResponse
import org.littlegit.client.engine.model.SignupRequest
import org.littlegit.client.testUtils.*
import org.mockito.Mockito.mock
import retrofit2.Call
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthControllerTests: BaseAsyncTest() {

    private lateinit var authController: AuthController
    private lateinit var authApi: AuthApi
    private lateinit var signupCall: Call<*>
    private lateinit var loginCall: Call<*>
    private lateinit var apiController: ApiController

    override fun setup() {
        super.setup()

        apiController = mock(ApiController::class.java)
        authApi = mock(AuthApi::class.java)
        signupCall = mock(Call::class.java)
        loginCall = mock(Call::class.java)

        upon(apiController.authApi).thenReturn(authApi)

        addToScope(apiController, ApiController::class)
        authController = findInTestScope(AuthController::class)

        upon(authApi.signup(any(SignupRequest::class))).thenReturn(signupCall as Call<Void>)
        upon(authApi.login(any(LoginRequest::class))).thenReturn(loginCall as Call<LoginResponse>)
    }

    @Test
    fun testSignup_WhenDetailsCorrect_GivesSuccess() = runTest { completion ->

        val loginResponse = LoginResponse("accessToken", "refreshToken", "Bearer", UserHelper.createUser())

        upon(signupCall.execute()).thenReturn(Response.success(Unit))
        upon(loginCall.execute()).thenReturn(Response.success(loginResponse))

        authController.signup("rob@stark.com", "W1nterfell!", "YoungWolf") { response ->
            assertTrue(response.isSuccess)
            assertEquals(loginResponse, response.body)

            completion()
        }
    }
}