package org.littlegit.client.controller

import com.squareup.moshi.Moshi
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import org.littlegit.client.engine.api.ErrorResponse
import org.littlegit.client.engine.serialization.MoshiProvider
import org.littlegit.client.testUtils.BaseAsyncTest

open class BaseControllerTest: BaseAsyncTest() {

    private lateinit var moshi: Moshi

    override fun setup() {
        super.setup()

        moshi = findInTestScope(MoshiProvider::class).moshi
    }

    fun serializeErrorResponse(error: ErrorResponse): ResponseBody =
            ResponseBody.create(MediaType.parse("application/json"), moshi.adapter(ErrorResponse::class.java).toJson(error))

}