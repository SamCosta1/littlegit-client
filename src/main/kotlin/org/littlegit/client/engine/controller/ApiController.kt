package org.littlegit.client.engine.controller

import okhttp3.OkHttpClient
import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.engine.api.AuthApi
import org.littlegit.client.engine.api.UserApi
import org.littlegit.client.env.EnvSwitcher
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tornadofx.*
import okhttp3.Interceptor
import okhttp3.Response
import org.littlegit.client.UnauthorizedEvent
import org.littlegit.client.engine.api.ApiResponse
import org.littlegit.client.engine.api.RepoApi
import org.littlegit.client.engine.model.AuthTokens
import org.littlegit.client.engine.model.RefreshResponse

open class ApiController: Controller() {

    open val authApi: AuthApi
    open val userApi: UserApi
    open val repoApi: RepoApi
    private val moshiProvider: MoshiProvider by inject()
    private val authController: AuthController by inject()

    init {
        val retrofit = buildLittlGitRetrofit()
        val authorizedRetrofit = buildAuthorizedRetrofit()

        authApi = retrofit.create(AuthApi::class.java)
        userApi = authorizedRetrofit.create(UserApi::class.java)
        repoApi = authorizedRetrofit.create(RepoApi::class.java)
    }

    private fun buildAuthorizedRetrofit(): Retrofit
            = Retrofit.Builder()
                .baseUrl(EnvSwitcher.currentEnvironment.baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshiProvider.moshi))
                .client(buildClient())
                .build()

    private fun buildClient(): OkHttpClient {

        return OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(authController, this)).build()
    }

    private fun buildLittlGitRetrofit(): Retrofit
            = Retrofit.Builder()
                .baseUrl(EnvSwitcher.currentEnvironment.baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshiProvider.moshi))
                .build()


}

interface AuthTokensProvider {
    val authTokens: AuthTokens?

    fun refreshToken(): ApiResponse<RefreshResponse>? { return null }
}

class AuthInterceptor(private val tokensProvider: AuthTokensProvider, val fireable: Component? = null): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val tokens = tokensProvider.authTokens

        if (tokens == null || tokens.accessToken.isEmpty()) {
            return chain.proceed(chain.request())
        }

        val authorisedRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${tokens.accessToken}").build()

        val result = chain.proceed(authorisedRequest)

        return if (result.code() == 401) {

            val refreshResponse = tokensProvider.refreshToken()
            val updatedResponse =  useRefreshedToken(refreshResponse, chain, result)

            // Refresh token and access token have expired
            if (updatedResponse.code() == 401) {
                fireable?.fire(UnauthorizedEvent)
            }

            updatedResponse

        } else {
            result
        }
    }

    private fun useRefreshedToken(refreshResponse: ApiResponse<RefreshResponse>?, chain: Interceptor.Chain, result: Response): Response {
        return if (refreshResponse?.isSuccess == true && refreshResponse.body?.accessToken != null) {
            val refreshedRequest = chain
                    .request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer ${refreshResponse.body.accessToken}")
                    .build()

            chain.proceed(refreshedRequest)
        } else {
            result
        }
    }
}