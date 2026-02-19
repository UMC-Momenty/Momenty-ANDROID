package com.example.momenty.domain.home

import android.util.Log
import com.example.momenty.global.security.TokenManager
import retrofit2.Response

class QuestRepository(
    private val service: QuestService,
    private val tokenManager: TokenManager
) {
    val TAG = "QuestRepository"

    suspend fun writeQuest(req: WriteQuestRequest): Result<String> =
        safeApiCall(
            apiCall = { service.writeQuest(req) },
            getResult = { it.result }
        )

    suspend fun loadQuest(): Result<LoadQuestData> =
        safeApiCall(
            apiCall = { service.loadQuest() },
            getResult = { it.result }
        )
    /*
    suspend fun writeQuest(accessToken: String, req: WriteQuestRequest)
    : Result<WriteQuestData> = try {
        val token = toBearerToken(accessToken)
        val response = service.writeQuest(token, req)

        if (response.isSuccessful) {
            val body = response.body()

            if (body == null) {
                Log.d(TAG, "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            }

            else if (body.result == null) {
                Log.d(TAG, "Response OK but Data is null")
                Result.failure(RuntimeException("Response OK but Data is null"))
            }
            else {
                Log.d(TAG, "OK")
                Result.success(body.result)
            }
        }
        else {
            val errMsg = response.errorBody()?.string() ?: response.message()
            Log.d(TAG, "비상: $errMsg")
            Result.failure(RuntimeException("HTTP ${response.code()}: $errMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun loadQuest(accessToken: String): Result<LoadQuestData> = try {
        val token = toBearerToken(accessToken)
        val response = service.loadQuest(token)

        if (response.isSuccessful) {
            val body = response.body()

            if (body == null) {
                Log.d(TAG, "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            }

            else if (body.result == null) {
                Log.d(TAG, "Response OK but Data is null")
                Result.failure(RuntimeException("Response OK but Data is null"))
            }
            else {
                Log.d(TAG, "OK")
                Result.success(body.result)
            }
        }
        else {
            val errMsg = response.errorBody()?.string() ?: response.message()
            Log.d(TAG, "비상: $errMsg")
            Result.failure(RuntimeException("HTTP ${response.code()}: $errMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }*/

    private suspend inline fun <T, R> safeApiCall(
        crossinline apiCall: suspend () -> Response<T>,
        crossinline getResult: (T) -> R?
    ): Result<R> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()

                if (body == null) {
                    Log.d(TAG, "Response body is null")
                    Result.failure(RuntimeException("Response body is null"))
                } else {
                    val result = getResult(body)
                    if (result == null) {
                        Log.d(TAG, "Response OK but Data is null")
                        Result.failure(RuntimeException("Response OK but Data is null"))
                    } else {
                        Log.d(TAG, "OK")
                        Result.success(result)
                    }
                }
            } else {
                val errMsg = response.errorBody()?.string() ?: response.message()
                Log.d(TAG, "비상: $errMsg")
                Result.failure(RuntimeException("HTTP ${response.code()}: $errMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun toBearerToken(accessToken: String): String {
        return if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
    }
}