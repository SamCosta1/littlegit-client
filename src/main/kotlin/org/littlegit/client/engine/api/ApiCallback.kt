package org.littlegit.client.engine.api

import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.serialization.MoshiProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tornadofx.*

typealias ApiCallCompletion<T> = (ApiResponse<T>) -> Unit
class ApiCallback<T>(private val completion: ApiCallCompletion<T>) : Callback<T> {

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            completion(ApiResponse.success(response.body()))
        } else {
            runAsync {
                parseError(response)
            } ui {
                completion(ApiResponse.error(it))
            }
        }
    }

    override fun onFailure(call: Call<T>?, throwable: Throwable?) {
        completion(ApiResponse.failure(throwable))
    }

    private fun parseError(response: Response<*>): CallFailure.ApiError? {
        val error = response.errorBody()?.string() ?: return null
        val moshi = find(MoshiProvider::class).moshi

        return try {
            moshi.adapter(CallFailure.ApiError::class.java).fromJson(error)
        } catch (ignored: Throwable) {
            null
        }
    }
}

fun <T>Call<T>.enqueue(completion: ApiCallCompletion<T>) {
    
    this.enqueue(ApiCallback(completion))

}

data class ApiResponse<T>(val isSuccess: Boolean, val body: T?, val errorBody: CallFailure?) {
    companion object {
        fun <T> failure(throwable: Throwable?): ApiResponse<T> = ApiResponse(false, null, CallFailure.ApiFailure(throwable))
        fun <T> success(body: T?): ApiResponse<T> = ApiResponse(true, body, null)
        fun <T> error(error: CallFailure.ApiError?): ApiResponse<T> = ApiResponse(false, null, error)
    }
}

sealed class CallFailure {
    data class ApiError(val errorCode: Int, val rawMessage: String, val localisedMessage: List<I18nKey>, val notes: List<String>): CallFailure()
    data class ApiFailure(val throwable: Throwable?): CallFailure()
}


