package com.example.momenty.domain.mypage

import android.util.Log
import retrofit2.Response

class MyPageRepository(private val service: MyPageService) {
    val TAG = "MyPageRepository"

    suspend fun loadProfile(accessToken: String, userId: Long): Result<LoadProfileData> =
        safeApiCall(
            apiCall = {service.loadProfile(toBearerToken(accessToken), userId)},
            getResult = {it.result}
        )

    suspend fun updateUserProfile(accessToken: String, userId: Long, req: UpdateUserProfileRequest): Result<Unit> =
        safeApiCall(
            apiCall = { service.updateUserProfile(toBearerToken(accessToken), userId, req) },
            getResult = { it.result }
        )

    suspend fun updatePetProfile(accessToken: String, userId: Long, petId: Int, req: UpdatePetProfileRequest): Result<Unit> =
        safeApiCall(
            apiCall = { service.updatePetProfile(toBearerToken(accessToken), userId, petId, req) },
            getResult = { it.result }
        )

    suspend fun addPetProfile(accessToken: String, userId: Long, req: AddPetProfileRequest): Result<Unit> =
        safeApiCall(
            apiCall = { service.addPetProfile(toBearerToken(accessToken), userId, req) },
            getResult = { it.result }
        )

    suspend fun loadNotice(accessToken: String): Result<LoadNoticeData<_NoticeData, _PageInfoData>> =
        safeApiCall(
            apiCall = { service.loadNotice(toBearerToken(accessToken)) },
            getResult = { it.result}
        )

    suspend fun loadNoticeDetail(accessToken: String, noticeId: Int): Result<LoadNoticeDetailData> =
        safeApiCall(
            apiCall = { service.loadNoticeDetail(toBearerToken(accessToken), noticeId) },
            getResult = { it.result}
        )

    suspend fun addInquiry(accessToken: String, userId: Long, req: AddInquiryRequest<AddInquiryRequestImg>): Result<String> =
        safeApiCall(
            apiCall = { service.addInquiry(toBearerToken(accessToken), userId, req) },
            getResult = { it.result}
        )

    suspend fun getImageUrl(accessToken: String, req: GetImageUrlRequest): Result<ArrayList<GetImageUrlData>> =
        safeApiCall(
            apiCall = { service.getImageUrl(toBearerToken(accessToken), req) },
            getResult = { it.result}
        )

    suspend fun loadInquiry(accessToken: String, userId: Long): Result<LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo>> =
        safeApiCall(
            apiCall = { service.loadInquiry(toBearerToken(accessToken), userId) },
            getResult = { it.result}
        )

    suspend fun loadInquiryDetail(accessToken: String, inquiryId: Int): Result<LoadInquiryDetailData<LoadInquiryDetailDataImages>> =
        safeApiCall(
            apiCall = { service.loadInquiryDetail(toBearerToken(accessToken), inquiryId) },
            getResult = { it.result}
        )

    suspend fun loadFaq(accessToken: String): Result<ArrayList<LoadFaqData>> =
        safeApiCall(
            apiCall = { service.loadFaq(toBearerToken(accessToken)) },
            getResult = { it.result}
        )

    suspend fun loadFaqDetail(accessToken: String, faqId: Int): Result<LoadFaqDetailData> =
        safeApiCall(
            apiCall = { service.loadFaqDetail(toBearerToken(accessToken), faqId) },
            getResult = { it.result}
        )


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



    /* suspend fun loadProfile(accessToken: String): Result<LoadProfileData<PetProfileData>> = try {
        val token = toBearerToken(accessToken)
        val response = service.loadProfile(token)

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

    suspend fun updateUserProfile(accessToken: String, req: UpdateUserProfileRequest): Result<Unit> = try {
        val token = toBearerToken(accessToken)
        val response = service.updateUserProfile(token, req)

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

    suspend fun updatePetProfile(accessToken: String, req: UpdatePetProfileRequest): Result<Unit> = try {
        val token = toBearerToken(accessToken)
        val response = service.updatePetProfile(token, req)

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

    suspend fun addPetProfile(accessToken: String, req: AddPetProfileRequest): Result<Unit> = try {
        val token = toBearerToken(accessToken)
        val response = service.addPetProfile(token, req)

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
}