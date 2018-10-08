package org.littlegit.client.engine.controller

import org.littlegit.client.engine.api.ApiCallCompletion
import org.littlegit.client.engine.api.AuthApi
import org.littlegit.client.engine.api.CallFailure
import org.littlegit.client.engine.api.enqueue
import org.littlegit.client.engine.db.AuthDb
import org.littlegit.client.engine.i18n.Localizer
import org.littlegit.client.engine.model.*
import tornadofx.*

class AuthController : Controller(), InitableController {

    var isLoggedIn: Boolean = false; get() = authTokens != null

    private val authApi: AuthApi = find(ApiController::class.java).authApi
    private val userController: UserController by inject()
    private val localizer: Localizer by inject()

    private var authTokens: AuthTokens? = null
    private val authDb: AuthDb by inject()

    override fun onStart(onReady: (InitableController) -> Unit) {
        authDb.getTokens {
            authTokens = it
            onReady(this)
        }
    }

    fun signup(email: String, password: String, name: String, completion: ApiCallCompletion<LoginResponse>) {
        val random = Math.random() * 10000
        val userName = "$name${random.toInt()}"
        authApi.signup(SignupRequest(email, password, name, "", localizer.currentLanguage.code, userName)).enqueue {

            // In the unlikely event we get a clashing username, try again
            if (it.errorBody is CallFailure.ApiError && it.errorBody.localisedMessage.contains(I18nKey.UsernameInUse)) {
                signup(email, password, name, completion)
            } else if (it.isSuccess) {
                login(email, password, completion)
            }
        }
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

    fun logout() {
        authDb.clearTokens()
        authTokens = null
    }
}