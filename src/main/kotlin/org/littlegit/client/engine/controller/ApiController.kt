package org.littlegit.client.engine.controller

import okhttp3.OkHttpClient
import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.engine.api.AuthApi
import org.littlegit.client.engine.api.UserApi
import org.littlegit.client.env.EnvSwitcher
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tornadofx.*
import com.sun.corba.se.spi.presentation.rmi.StubAdapter.request
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException



class ApiController: Controller() {

    val authApi: AuthApi
    val userApi: UserApi
    private val moshiProvider: MoshiProvider by inject()
    lateinit var authController: AuthController

    init {
        val retrofit = buildLittlGitRetrofit()
        val authorizedRetrofit = buildAuthorizedRetrofit()

        authApi = retrofit.create(AuthApi::class.java)
        userApi = authorizedRetrofit.create(UserApi::class.java)
    }

    private fun buildAuthorizedRetrofit(): Retrofit
            = Retrofit.Builder()
                .baseUrl(EnvSwitcher.currentEnvironment.baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshiProvider.moshi))
                .client(buildClient())
                .build()

    private fun buildClient(): OkHttpClient {

        return OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val tokens = authController.authTokens

                    if (tokens == null || tokens.accessToken.isEmpty()) {
                        return@Interceptor chain.proceed(chain.request())
                    }

                    val authorisedRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ${tokens.accessToken}").build()

                    val result = chain.proceed(authorisedRequest)

                    if (result.code() == 401) {
                        val refreshResponse = authController.refreshToken()

                        if (refreshResponse?.isSuccess == true && refreshResponse.body?.accessToken != null) {
                            val refreshedRequest = chain
                                    .request()
                                    .newBuilder()
                                    .addHeader("Authorization", "Bearer ${refreshResponse.body.accessToken}")
                                    .build()

                            chain.proceed(refreshedRequest)
                        } else {
                            result
                        }
                    } else {
                        result
                    }

                }).build()
    }

    private fun buildLittlGitRetrofit(): Retrofit
            = Retrofit.Builder()
                .baseUrl(EnvSwitcher.currentEnvironment.baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshiProvider.moshi))
                .build()


}