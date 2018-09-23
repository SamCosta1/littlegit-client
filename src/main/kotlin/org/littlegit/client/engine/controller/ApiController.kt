package org.littlegit.client.engine.controller

import com.squareup.moshi.Moshi
import org.littlegit.client.engine.api.AuthApi
import org.littlegit.client.env.EnvSwitcher
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tornadofx.*

class ApiController: Controller() {

    val authApi: AuthApi

    init {
        val retrofit = buildLittlGitRetrofit()

        authApi = retrofit.create(AuthApi::class.java)
    }

    private fun buildLittlGitRetrofit(): Retrofit
            = Retrofit.Builder()
                .baseUrl(EnvSwitcher.currentEnvironment.baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

    private val moshi: Moshi = buildMoshi()

    private fun buildMoshi(): Moshi = Moshi.Builder().build()
}