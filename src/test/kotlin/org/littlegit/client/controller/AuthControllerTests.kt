package org.littlegit.client.controller

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.littlegit.client.engine.api.*
import org.littlegit.client.engine.controller.ApiController
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.model.LoginRequest
import org.littlegit.client.engine.model.LoginResponse
import org.littlegit.client.engine.model.SignupRequest
import org.littlegit.client.testUtils.*
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthControllerTests: BaseControllerTest() {

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
    fun testSignup_WhenDetailsCorrect_DoesLoginAndGivesSuccess() = runTest { completion ->

        val loginResponse = LoginResponse("accessToken", "refreshToken", "Bearer", UserHelper.createUser())

        upon(signupCall.execute()).thenReturn(Response.success(Unit))
        upon(loginCall.execute()).thenReturn(Response.success(loginResponse))

        val email = "rob@stark.com"
        val password = "W1nterfell!"
        authController.signup(email, password, "YoungWolf") { response ->
            assertTrue(response.isSuccess)
            assertEquals(loginResponse, response.body)
            completion()
        }
    }

    @Test
    fun testSignup_WhenDetailsInvalid_GivesError() = runTest { completion ->

        val error = ErrorResponse("Bad Request", listOf(I18nKey.InvalidEmail))
        upon(signupCall.execute()).thenReturn(Response.error<Unit>(400, serializeErrorResponse(error)))

        authController.signup("arya@stark.com", "F@celess!", "Arya123") { response ->
            assertFalse(response.isSuccess)
            assertNull(response.body)

            val errorBody = response.errorBody
            assertTrue(errorBody is CallFailure.ApiError); errorBody as CallFailure.ApiError
            assertEquals(error.rawMessage, errorBody.rawMessage)
            assertEquals(1, errorBody.localisedMessage.size)
            assertEquals(error.localisedMessage.first(), errorBody.localisedMessage.first())

            completion()
        }
    }
}