package org.littlegit.client.engine.api

import org.littlegit.client.engine.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST
    fun refreshToken(@Body refreshRequest: RefreshRequest): Call<RefreshResponse>

    @POST("/user/signup")
    fun signup(@Body signupDetails: SignupRequest): Call<Void>
}