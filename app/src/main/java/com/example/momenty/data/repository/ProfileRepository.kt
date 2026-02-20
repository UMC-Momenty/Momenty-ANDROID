package com.example.momenty.data.repository

import android.util.Log
import com.example.momenty.data.remote.profile.AddPetRequest
import com.example.momenty.data.remote.profile.PetApi
import com.example.momenty.data.remote.profile.ProfileApi
import com.example.momenty.data.remote.profile.UpdatePetProfileRequest
import com.example.momenty.data.remote.profile.UpdateProfileRequest
import com.example.momenty.global.security.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val petApi: PetApi,
    private val tokenManager: TokenManager
) {

    /**
     * 프로필 업데이트 결과
     */
    sealed class ProfileResult {
        data class Success(
            val accessToken: String?,
            val refreshToken: String?
        ) : ProfileResult()

        sealed class Error(open val message: String) : ProfileResult() {
            data class Network(override val message: String) : Error(message)
            data class Api(override val message: String, val code: String) : Error(message)
            data class Unauthorized(override val message: String = "인증이 만료되었습니다") : Error(message)
            data class Unknown(override val message: String, val throwable: Throwable? = null) : Error(message)
        }
    }

    /**
     * 사용자 및 반려동물 프로필 업데이트
     */
    suspend fun updateUserProfile(
        username: String? = null,
        gender: String? = null,
        birth: String? = null,
        profileUrl: String? = null,
        questTime: String? = null,
        resetQuestTime: Boolean? = null
    ): ProfileResult = withContext(Dispatchers.IO) {
        try {
            // 액세스 토큰 확인
            val accessToken = tokenManager.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                return@withContext ProfileResult.Error.Unauthorized()
            }

            val request = UpdateProfileRequest(
                username = username,
                gender = gender,
                birth = birth,
                profileUrl = profileUrl,
                questTime = questTime,
                resetQuestTime = resetQuestTime
            )

            val response = profileApi.updateProfile(request)

            if (response.isSuccess) {
                ProfileResult.Success(
                    accessToken = response.result?.accessToken,
                    refreshToken = response.result?.refreshToken
                )
            } else {
                ProfileResult.Error.Api(
                    message = response.message ?: "프로필 업데이트 실패",
                    code = response.code ?: "UNKNOWN"
                )
            }



        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error: ${e.code()}", e)
            when (e.code()) {
                401 -> ProfileResult.Error.Unauthorized()
                else -> ProfileResult.Error.Api(
                    message = "서버 오류가 발생했습니다",
                    code = e.code().toString()
                )
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error", e)
            ProfileResult.Error.Network("네트워크 연결을 확인해주세요")
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            ProfileResult.Error.Network("요청 시간이 초과되었습니다")
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error", e)
            ProfileResult.Error.Unknown(
                message = e.message ?: "알 수 없는 오류가 발생했습니다",
                throwable = e
            )
        }
    }

    /**
     * 반려동물 프로필 수정 - PATCH /api/users/{userId}/pets/{petId}
     * 전달되지 않은 필드는 기존 값 유지
     * 빈 배열이면 사진, 소개글 미설정
     */
    suspend fun updatePetProfile(
        petId: Long,
        profileImageUrl: String? = null,
        petName: String? = null,
        gender: String? = null,
        birth: String? = null,
        species: String? = null,    // "CAT" or "DOG"
        breedId: Long? = null,
        intro: String? = null
    ): ProfileResult = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                return@withContext ProfileResult.Error.Unauthorized()
            }

            val request = UpdatePetProfileRequest(
                profileImageUrl = profileImageUrl,
                petName = petName,
                gender = gender,
                birth = birth,
                species = species,
                breedId = breedId,
                intro = intro
            )

            val response = petApi.updatePetProfile(petId, request)

            if (response.isSuccess) {
                // 반려동물 수정은 토큰 반환 없음
                ProfileResult.Success(
                    accessToken = null,
                    refreshToken = null
                )
            } else {
                ProfileResult.Error.Api(
                    message = response.message ?: "반려동물 프로필 업데이트 실패",
                    code = response.code ?: "UNKNOWN"
                )
            }

        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error: ${e.code()}", e)
            when (e.code()) {
                401 -> ProfileResult.Error.Unauthorized()
                404 -> ProfileResult.Error.Api(
                    message = "존재하지 않는 반려동물 또는 사용자입니다",
                    code = "404"
                )
                else -> ProfileResult.Error.Api(
                    message = "서버 오류가 발생했습니다",
                    code = e.code().toString()
                )
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error", e)
            ProfileResult.Error.Network("네트워크 연결을 확인해주세요")
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            ProfileResult.Error.Network("요청 시간이 초과되었습니다")
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error", e)
            ProfileResult.Error.Unknown(
                message = e.message ?: "알 수 없는 오류가 발생했습니다",
                throwable = e
            )
        }
    }

    suspend fun addPet(
        profileImageUrl: String? = null,
        petName: String,
        gender: String,
        birth: String,
        species: String,
        breedId: Long? = null,
        intro: String? = null
    ): ProfileResult = withContext(Dispatchers.IO) {
        try {
            val accessToken = tokenManager.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                return@withContext ProfileResult.Error.Unauthorized()
            }

            val request = AddPetRequest(
                profileImageUrl = profileImageUrl,
                petName = petName,
                gender = gender,
                birth = birth,
                species = species,
                breedId = breedId,
                intro = intro
            )

            val response = petApi.addPet(request)

            if (response.isSuccess) {
                ProfileResult.Success(accessToken = null, refreshToken = null)
            } else {
                ProfileResult.Error.Api(
                    message = response.message ?: "반려동물 추가 실패",
                    code = response.code ?: "UNKNOWN"
                )
            }
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                401 -> ProfileResult.Error.Unauthorized()
                else -> ProfileResult.Error.Api("서버 오류", e.code().toString())
            }
        } catch (e: java.net.UnknownHostException) {
            ProfileResult.Error.Network("네트워크 연결을 확인해주세요")
        } catch (e: java.net.SocketTimeoutException) {
            ProfileResult.Error.Network("요청 시간이 초과되었습니다")
        } catch (e: Exception) {
            ProfileResult.Error.Unknown(e.message ?: "알 수 없는 오류", e)
        }
    }

    companion object {
        private const val TAG = "ProfileRepository"
    }
}