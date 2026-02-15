package com.example.momenty.domain.home

import android.util.Log

class QuestRepository(private val service: QuestService) {
    val TAG = "QuestRepository"
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
    }

    private fun toBearerToken(accessToken: String): String {
        return if (accessToken.startsWith("Bearer ")) accessToken else "Bearer $accessToken"
    }
}