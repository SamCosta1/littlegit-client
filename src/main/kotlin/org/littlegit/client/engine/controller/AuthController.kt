package org.littlegit.client.engine.controller

import org.littlegit.client.engine.api.ApiCallCompletion
import org.littlegit.client.engine.api.AuthApi
import org.littlegit.client.engine.api.enqueue
import org.littlegit.client.engine.db.AuthDb
import org.littlegit.client.engine.model.AuthTokens
import org.littlegit.client.engine.model.LoginRequest
import org.littlegit.client.engine.model.LoginResponse
import tornadofx.*

class AuthController : Controller(), InitableController {

    var isLoggedIn: Boolean = false; get() = authTokens != null

    private val authApi: AuthApi = find(ApiController::class.java).authApi
    private val userController: UserController by inject()

    private var authTokens: AuthTokens? = null
    private val authDb: AuthDb by inject()

    override fun onStart(onReady: (InitableController) -> Unit) {
        authDb.getTokens {
            authTokens = it
            onReady(this)
        }
    }

    fun signup(completion: ApiCallCompletion<Void>) {
        // authApi.signup(LoginRequest(email, password)).enqueue(completion)
    }

    fun login(email: String, password: String, completion: ApiCallCompletion<LoginResponse>) {
        authApi.login(LoginRequest(email, password)).enqueue {
            if (it.isSuccess && it.body != null) {
                updateAuthTokens(it.body.accessToken, it.body.refreshToken)
                userController.updateUserCache(it.body.user)
            }

            completion(it)
        }
    }

    private fun updateAuthTokens(accessToken: String, refreshToken: String) {
        val authTokens = AuthTokens(accessToken, refreshToken)
        authDb.updateTokens(authTokens)
        this.authTokens = authTokens
    }
}