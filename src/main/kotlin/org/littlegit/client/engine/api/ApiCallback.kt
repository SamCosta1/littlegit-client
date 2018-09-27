package org.littlegit.client.engine.api

import org.littlegit.client.engine.model.I18nKey
import org.littlegit.client.engine.serialization.MoshiProvider
import retrofit2.Call
import retrofit2.Response
import tornadofx.*

typealias ApiCallCompletion<T> = (ApiResponse<T>) -> Unit

private fun parseError(response: Response<*>): CallFailure.ApiError? {
    val error = response.errorBody()?.string() ?: return null
    val moshi = find(MoshiProvider::class).moshi

    return try {
        val errorResponse = moshi.adapter(ErrorResponse::class.java).fromJson(error) ?: ErrorResponse()
        CallFailure.ApiError(response.code(), errorResponse.rawMessage, errorResponse.localisedMessage, errorResponse.notes)
    } catch (ignored: Throwable) {
        null
    }
}

fun <T>Call<T>.enqueue(completion: ApiCallCompletion<T>) {
    runAsync {

        val response: Response<T>

        try {
            response = this@enqueue.execute()
        } catch (e: Throwable) {
            return@runAsync ApiResponse.failure<T>(e)
        }


        if (response.isSuccessful) {
            return@runAsync ApiResponse.success(response.body())
        } else {
            return@runAsync ApiResponse.error<T>(parseError(response))
        }
    } ui {
        completion(it)
    }
}

data class ApiResponse<T>(val isSuccess: Boolean, val body: T?, val errorBody: CallFailure?) {
    companion object {
        fun <T> failure(throwable: Throwable?): ApiResponse<T> = ApiResponse(false, null, CallFailure.ApiFailure(throwable))
        fun <T> success(body: T?): ApiResponse<T> = ApiResponse(true, body, null)
        fun <T> error(error: CallFailure.ApiError?): ApiResponse<T> = ApiResponse(false, null, error)
    }
}

private data class ErrorResponse(val rawMessage: String = "", val localisedMessage: List<I18nKey> = emptyList(), val notes: List<String> = emptyList())
sealed class CallFailure {
    data class ApiError(val errorCode: Int, val rawMessage: String, val localisedMessage: List<I18nKey>, val notes: List<String>): CallFailure()
    data class ApiFailure(val throwable: Throwable?): CallFailure()
}


