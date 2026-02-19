package com.example.momenty.domain.mypage

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.momenty.data.remote.moment.CreateMomentRequestDto
import com.example.momenty.data.remote.moment.MomentImageKeyDto
import com.example.momenty.data.remote.moment.PresignedRequestDto
import com.example.momenty.global.security.TokenManager
import retrofit2.Response
import kotlin.collections.forEachIndexed
import kotlin.collections.map
import kotlin.collections.orEmpty
import kotlin.collections.take

class MyPageRepository(
    private val service: MyPageService
) {
    val TAG = "MyPageRepository"

    // ===================== pet-controller =====================

    /**
     * 반려동물 프로필 추가
     */
    suspend fun addPetProfile(req: AddPetProfileRequest): Result<String?> =
        safeApiCall(
            apiCall = { service.addPetProfile(req) },
            getResult = { it.result }
        )

    /**
     * 특정 반려동물 프로필 조회
     */
    suspend fun loadOnePetProfile(petId: Long): Result<LoadOnePetProfileData> =
        safeApiCall(
            apiCall = {service.loadOnePetProfile(petId)},
            getResult = {it.result}
        )

    /**
     * 반려동물 프로필 수정
     */
    suspend fun updatePetProfile(petId: Long, req: UpdatePetProfileRequest): Result<String?> =
        safeApiCall(
            apiCall = { service.updatePetProfile(petId, req) },
            getResult = { it.result }
        )

    /**
     * 내 반려동물 리스트 조회
     */
    suspend fun loadPetList(): Result<ArrayList<LoadPetListData>> =
        safeApiCall(
            apiCall = { service.loadPetList() },
            getResult = { it.result }
        )


    // ===================== inquiry-controller =====================

    /**
     * 문의하기
     */
    suspend fun addInquiry(type: String, content: String, /*req: AddInquiryRequest<AddInquiryRequestImg>, */imageUris: ArrayList<Uri>?, contentResolver: ContentResolver): Result<String?> {
        lateinit var body: AddInquiryRequest<AddInquiryRequestImg>
        if (!imageUris.isNullOrEmpty()) {
            val safeUris = imageUris.take(2)
            val imageCount = safeUris.size

            val mimeTypes = safeUris.map { uri ->
                contentResolver.getType(uri) ?: "image/jpeg"
            }

            val imageTypes = mimeTypes.map { mime ->
                if (mime == "image/png") "PNG" else "JPEG"
            }

            val imgReq = GetImageUrlRequest(
                ArrayList(imageTypes)
            )

            val presignedRes = service.createInquiryPresignedUrls(imgReq)
            val presigned = presignedRes.body()?.result

            if (!presignedRes.isSuccessful) {
                throw IllegalStateException("presigned 발급 실패")
            }
            if (presigned?.size != imageCount) {
                Log.e(TAG, "presigned:${presigned?.size}, imageCount:${imageCount}")
                Log.e(TAG, "safeUris: $safeUris")
                Log.e(TAG, "presigned: $presigned")
                throw IllegalStateException("presigned 개수 불일치")
            }



            if (!com.example.momenty.global.mock.MockApiInterceptor.isMockEnabled) {
                presigned?.forEachIndexed { idx, item ->
                    val uri = safeUris[idx]
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IllegalStateException("이미지 읽기 실패: $uri")

                    val contentType = mimeTypes[idx]

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.example.momenty.data.remote.S3Uploader.upload(
                            presignedUrl = item.url,
                            bytes = bytes,
                            contentType = contentType
                        )
                    }
                }
            } else {
                android.util.Log.d("MockApi", "SKIP S3 upload (mock mode)")
            }

            body = AddInquiryRequest(
                type, content, ArrayList(presigned!!.map { AddInquiryRequestImg(0L, it.key)})
            )
        } else {
            body = AddInquiryRequest(
                type, content, null
            )
        }

        //if (imageUris!!.isEmpty()) throw IllegalArgumentException("사진 1장 이상 필요")


        return try {
            val response = service.addInquiry(body)

            if (response.isSuccessful) {
                val resBody = response.body()

                if (resBody == null) {
                    Log.d(TAG, "Response Body is null")
                    Result.failure(RuntimeException("Response Body is null"))
                } else if(resBody.result == null) {
                    Log.d(TAG, "Response OK but Data is null")
                    Result.failure(RuntimeException("Response OK but Data is null"))
                } else {
                    Log.d(TAG, "OK")
                    Result.success(resBody.result)
                }
            } else {
                val errMsg = response.errorBody()?.string() ?: response.message()
                Log.d(TAG, "비상: $errMsg")
                Result.failure(RuntimeException("HTTP ${response.code()}: $errMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /*=
        safeApiCall(
            apiCall = { service.addInquiry(tokenManager.getUserId(), req) },
            getResult = { it.result}
        )*/

    /**
     * 문의하기 이미지 업로드용 Presigned URL 발급
     */
    suspend fun createInquiryPresignedUrls(req: GetImageUrlRequest): Result<ArrayList<GetImageUrlData>> =
        safeApiCall(
            apiCall = { service.createInquiryPresignedUrls(req) },
            getResult = { it.result}
        )

    /**
     * 문의내역 상세 조회
     */
    suspend fun loadInquiryDetail(inquiryId: Long): Result<LoadInquiryDetailData<LoadInquiryDetailDataImages>> =
        safeApiCall(
            apiCall = { service.loadInquiryDetail(inquiryId) },
            getResult = { it.result}
        )

    /**
     * 문의내역 리스트 조회
     */
    suspend fun loadInquiry(page: Int, size: Int, sort: ArrayList<String>): Result<LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo>> =
        safeApiCall(
            apiCall = { service.loadInquiry(page, size, sort) },
            getResult = { it.result}
        )



    // ===================== user-controller =====================

    /**
     * 사용자 프로필 조회
     */
    suspend fun loadProfile(): Result<LoadProfileData> =
        safeApiCall(
            apiCall = {service.loadProfile()},
            getResult = {it.result}
        )

    /**
     * 사용자 프로필 수정
     */
    suspend fun updateUserProfile(req: UpdateUserProfileRequest): Result<UpdateUserProfileData> =
        safeApiCall(
            apiCall = { service.updateUserProfile(req) },
            getResult = { it.result }
        )

    /**
     * 사용자 상세 조회
     */
    suspend fun loadProfileDetail(): Result<LoadProfileDetailData> =
        safeApiCall(
            apiCall = {service.loadProfileDetail()},
            getResult = {it.result}
        )



    // ===================== notice-controller =====================

    /**
     * 공지사항 리스트 조회
     */
    suspend fun loadNotice(page: Int, size: Int, sort: ArrayList<String>): Result<LoadNoticeData<_NoticeDatas, _PageInfoData>> =
        safeApiCall(
            apiCall = { service.loadNotice(page, size, sort) },
            getResult = { it.result}
        )

    /**
     * 공지사항 상세 조회
     */
    suspend fun loadNoticeDetail(noticeId: Long): Result<LoadNoticeDetailData> =
        safeApiCall(
            apiCall = { service.loadNoticeDetail(noticeId) },
            getResult = { it.result}
        )



    // ===================== faq-controller =====================

    /**
     * FAQ 리스트 조회
     */
    suspend fun loadFaq(): Result<ArrayList<LoadFaqData>> =
        safeApiCall(
            apiCall = { service.loadFaq() },
            getResult = { it.result}
        )

    /**
     * FAQ 상세 조회
     */
    suspend fun loadFaqDetail(faqId: Long): Result<LoadFaqDetailData> =
        safeApiCall(
            apiCall = { service.loadFaqDetail(faqId) },
            getResult = { it.result}
        )



    // ===================== UNKNOWN =====================

    suspend fun logout(): Result<String?> = try {
        val response = service.logout()

        if (response.isSuccessful) {
            val body = response.body()

            if (body == null) {
                Log.d(TAG, "Response body is null")
                Result.failure(RuntimeException("Response body is null"))
            }
            else if (body.result == null) {
                Log.d(TAG, "Response OK but Data is null")
                Result.success(body.result)
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

    /*
    suspend fun loadPetsProfile(): Result<ArrayList<LoadOnePetProfileData>> =
        safeApiCall(
            apiCall = {service.loadPetsProfile(tokenManager.getUserId())},
            getResult = {it.result}
        )*/


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