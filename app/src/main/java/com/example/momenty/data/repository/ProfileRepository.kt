package com.example.momenty.data.repository

import android.util.Log
import com.example.momenty.data.remote.profile.ProfileApi
import com.example.momenty.data.remote.profile.UpdateProfileRequest
import com.example.momenty.global.security.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val tokenManager: TokenManager
) {

    /**
     * 프로필 업데이트 결과
     */
    sealed class ProfileResult {
        data class Success(
            val userId: String,
            val userName: String,
            val petId: String,
            val petName: String
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
    suspend fun updateProfile(
        userName: String,
        userGender: String,
        userBirthDate: String,
        userImageKey: String?,      // 변경: URL → Key
        alarmTime: String?,
        petName: String,
        petGender: String,
        petBirthDate: String,
        petImageKey: String?,       // 변경: URL → Key
        petType: String,
        petBreed: String?,
        petIntroduction: String?
    ): ProfileResult = withContext(Dispatchers.IO) {
        try {
            // 액세스 토큰 확인
            val accessToken = tokenManager.getAccessToken()
            if (accessToken.isNullOrEmpty()) {
                return@withContext ProfileResult.Error.Unauthorized()
            }

            val request = UpdateProfileRequest(
                userName = userName,
                userGender = userGender,
                userBirthDate = userBirthDate,
                userImageKey = userImageKey,    // Key 전달
                alarmTime = alarmTime,
                petName = petName,
                petGender = petGender,
                petBirthDate = petBirthDate,
                petImageKey = petImageKey,      // Key 전달
                petType = petType,
                petBreed = petBreed,
                petIntroduction = petIntroduction
            )

            val response = profileApi.updateProfile(request)

            if (response.isSuccess) {
                response.result?.let { data ->
                    ProfileResult.Success(
                        userId = data.userId,
                        userName = data.userName,
                        petId = data.petId,
                        petName = data.petName
                    )
                } ?: ProfileResult.Error.Unknown("응답 데이터가 없습니다")
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

    companion object {
        private const val TAG = "ProfileRepository"
    }
}