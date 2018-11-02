package org.littlegit.client.engine.api

import org.littlegit.client.engine.model.SignupRequest
import org.littlegit.client.engine.model.SshKeyRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("/user/add-ssh-key")
    fun addSshKey(@Body request: SshKeyRequest): Call<Void>
}
