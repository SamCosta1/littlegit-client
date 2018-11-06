package org.littlegit.client.controller

import org.junit.Test
import org.littlegit.client.engine.api.*
import org.littlegit.client.engine.controller.ApiController
import org.littlegit.client.engine.controller.AuthController
import org.littlegit.client.engine.db.AuthDb
import org.littlegit.client.engine.model.*
import org.littlegit.client.testUtils.*
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Response
import tornadofx.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthControllerTests: BaseControllerTest() {

    private lateinit var authController: AuthController
    private lateinit var authApi: AuthApi
    private lateinit var authDb: AuthDb
    private lateinit var signupCall: Call<*>
    private lateinit var loginCall: Call<*>
    private lateinit var apiController: ApiController

    override fun setup() {
        super.setup()

        apiController = mock(ApiController::class.java)
        authApi = mock(AuthApi::class.java)
        authDb = mock(AuthDb::class.java)
        signupCall = mock(Call::class.java)
        loginCall = mock(Call::class.java)

        upon(apiController.authApi).thenReturn(authApi)

        addToScope(apiController, ApiController::class)
        addToScope(authDb, AuthDb::class)
        authController = findInTestScope(AuthController::class)

        upon(authApi.signup(anyOf(SignupRequest::class))).thenReturn(signupCall as Call<Void>)
        upon(authApi.login(anyOf(LoginRequest::class))).thenReturn(loginCall as Call<LoginResponse>)
    }

    @Test
    fun testSignup_WhenDetailsCorrect_DoesLoginAndGivesSuccess() = runTest { completion ->

        val loginResponse = LoginResponse("accessToken", "refreshToken", "Bearer", UserHelper.createUser())

        upon(signupCall.execute()).thenReturn(Response.success(Unit))
        upon(loginCall.execute()).thenReturn(Response.success(loginResponse))
        upon(authDb.updateTokens(anyOf(AuthTokens::class), any())).thenReturn(runAsync {  })

        val email = "rob@stark.com"
        val password = "W1nterfell!"
        authController.signup(email, password, "YoungWolf") { response ->
            assertTrue(response.isSuccess)
            assertEquals(loginResponse, response.body)
            verify(authDb, times(1)).updateTokens(anyOf(AuthTokens::class), any())
            verify(authApi, times(1)).signup(anyOf(SignupRequest::class))
            verify(authApi, times(1)).login(anyOf(LoginRequest::class))
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

            verify(authApi, times(1)).signup(anyOf(SignupRequest::class))
            verify(authApi, times(0)).login(anyOf(LoginRequest::class))

            completion()
        }
    }

    @Test
    fun testSignup_WhenDuplicateUserName_TriesAgainWithNewUserName() = runTest { completion ->
        val loginResponse = LoginResponse("accessToken", "refreshToken", "Bearer", UserHelper.createUser())
        val initialError = ErrorResponse("Bad Request", listOf(I18nKey.UsernameInUse))

        val initialErrResponse = Response.error<Unit>(400, serializeErrorResponse(initialError))
        upon(signupCall.execute()).thenReturn(initialErrResponse, Response.success(Unit))
        upon(loginCall.execute()).thenReturn(Response.success(loginResponse))
        upon(authDb.updateTokens(anyOf(AuthTokens::class), any())).thenReturn(runAsync {  })

        val email = "rob@stark.com"
        val password = "W1nterfell!"
        authController.signup(email, password, "YoungWolf") { response ->
            assertTrue(response.isSuccess)
            assertEquals(loginResponse, response.body)
            verify(authDb, times(1)).updateTokens(anyOf(AuthTokens::class), any())
            verify(authApi, times(2)).signup(anyOf(SignupRequest::class))
            verify(authApi, times(1)).login(anyOf(LoginRequest::class))
            completion()
        }
    }

    @Test
    fun testLogin_WhenValidCredentials_IsSuccessful() = runTest { completion ->
        val loginResponse = LoginResponse("accessToken", "refreshToken", "Bearer", UserHelper.createUser())

        upon(loginCall.execute()).thenReturn(Response.success(loginResponse))
        upon(authDb.updateTokens(anyOf(AuthTokens::class), any())).thenReturn(runAsync {  })

        val email = "rob@stark.com"
        val password = "W1nterfell!"
        authController.login(email, password) { response ->
            assertTrue(response.isSuccess)
            assertEquals(loginResponse, response.body)
            verify(authDb, times(1)).updateTokens(anyOf(AuthTokens::class), any())
            verify(authApi, times(1)).login(anyOf(LoginRequest::class))
            completion()
        }
    }
}