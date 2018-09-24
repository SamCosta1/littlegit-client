package org.littlegit.client.engine.controller

import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.engine.api.AuthApi
import org.littlegit.client.env.EnvSwitcher
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tornadofx.*

class ApiController: Controller() {

    val authApi: AuthApi
    val moshiProvider: MoshiProvider by inject()

    init {
        val retrofit = buildLittlGitRetrofit()

        authApi = retrofit.create(AuthApi::class.java)
    }

    private fun buildLittlGitRetrofit(): Retrofit
            = Retrofit.Builder()
                .baseUrl(EnvSwitcher.currentEnvironment.baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshiProvider.moshi))
                .build()


}