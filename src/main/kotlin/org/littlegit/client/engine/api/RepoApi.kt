package org.littlegit.client.engine.api

import org.littlegit.client.engine.model.CreateRepoRequest
import org.littlegit.client.engine.model.RemoteRepoSummary
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RepoApi {
    @POST("/repo/create")
    fun createRemoteRepo(@Body request: CreateRepoRequest): Call<RemoteRepoSummary>

    @GET("repo/repos")
    fun getAllRepos(): Call<List<RemoteRepoSummary>>
}
