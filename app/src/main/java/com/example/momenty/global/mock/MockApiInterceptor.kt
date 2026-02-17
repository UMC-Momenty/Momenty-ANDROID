package com.example.momenty.global.mock

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.UUID


/**
 * Mock API Interceptor
 * 백엔드 없이 테스트하기 위한 가짜 API 응답 생성
 * SharedPreferences에 저장된 실제 데이터를 사용
 *
 * 사용법:
 * 1. NetworkModule에서 이 Interceptor를 OkHttpClient에 추가
 * 2. BuildConfig.USE_MOCK_API = true로 설정
 * 3. initialize(context)를 호출하여 Context 설정
 */
class MockApiInterceptor : Interceptor {

    companion object {
        // Mock 모드 활성화 여부 (build.gradle에서 설정)
        var isMockEnabled = false

        // Mock 지연 시간 (ms)
        private const val MOCK_DELAY = 500L

        // Context 참조 (SharedPreferences 접근용)
        private var appContext: Context? = null

        // Gson 인스턴스
        private val gson = Gson()

        /**
         * Context 초기화 (Application에서 호출)
         */
        fun initialize(context: Context) {
            appContext = context.applicationContext
        }

        /**
         * SharedPreferences 가져오기
         */
        private fun getPrefs(): SharedPreferences? {
            return appContext?.getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isMockEnabled) {
            // Mock 비활성화 시 실제 API 호출
            return chain.proceed(chain.request())
        }

        // Mock 딜레이 추가 (네트워크 시뮬레이션)
        Thread.sleep(MOCK_DELAY)

        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method

        android.util.Log.d("MockApi", "REQ: $method $path (mock=$isMockEnabled)")


        // API 경로별 Mock 응답 생성
        val mockResponse = when {
            // ==================== 인증 API ====================
            path.endsWith("/api/oauth/login") && method == "POST" -> {
                mockSocialLogin()
            }
            path.endsWith("/api/oauth/reissue") && method == "POST" -> {
                mockTokenReissue()
            }

            // ==================== 프로필 API ====================
            path.endsWith("/api/mypage") && method == "GET" -> {
                mockGetProfile()
            }
            path.endsWith("/api/mypage") && method == "PATCH" -> {
                mockUpdateProfile()
            }

            // ==================== 이미지 업로드 API ====================
            path.endsWith("/api/profile/image") && method == "POST" -> {
                mockCreatePresignedUrl()
            }

            // ==================== 일정 관리 API ====================
            path.contains("/api/schedules") && method == "GET" -> {
                mockGetSchedules()
            }
            path.endsWith("/api/schedules") && method == "POST" -> {
                mockCreateSchedule()
            }

            // ==================== 오늘의 질문 API ====================
            path.endsWith("/api/quest/today") && method == "GET" -> {
                mockGetTodayQuestion()
            }
            path.endsWith("/api/quest") && method == "POST" -> {
                mockSubmitAnswer()
            }
            path.endsWith("/api/quest/today/status") && method == "GET" -> {
                mockGetQuestionStatus()
            }

            // ==================== 모먼트 API ====================


            // /api/moments/users/{userId}/pets/{petId}
            path.matches(Regex(".*/api/moments/users/\\d+/pets/\\d+")) && method == "GET" -> {
                Log.d("MockApi", "HIT momentsByPet")
                mockGetMomentsByPet()
            }


            path.matches(Regex("/api/moments/users/\\d+/pets/\\d+")) && method == "POST" -> {
                Log.d("MockApi", "HIT createMomentByPet")
                mockCreateMomentByPet()
            }



            path.endsWith("/api/moments/image") && method == "POST" -> {
                android.util.Log.d("MockApi", "HIT momentsPresigned")
                mockCreateMomentPresignedUrls()
            }

            path.matches(Regex(".*/api/moments/users/\\d+/pets/\\d+/\\d+")) && method == "GET" -> {
                Log.d("MockApi", "HIT momentDetail")
                mockGetMomentDetail()
            }





            // ==================== 챗봇 API ====================
            path.endsWith("/api/chatbot/messages") && method == "POST" -> {
                mockSendChatMessage()
            }
            path.endsWith("/api/chatbot/conversations") && method == "GET" -> {
                mockGetConversations()
            }

            // ==================== 공지사항 API ====================
            path.endsWith("/api/notice") && method == "GET" -> {
                mockGetNotices()
            }

            // ==================== 반려동물 API ====================
            path.endsWith("/api/pets") && method == "GET" -> {
                mockGetPets()
            }

            // ==================== 캘린더 API ====================
            // 사용자의 반려동물 목록 조회 (일정 관리용)
            path.matches(Regex(".*/api/users/\\d+/schedules/pets")) && method == "GET" -> {
                mockGetUserPets()
            }

            // 모든 반려동물 월별 일정 조회
            path.matches(Regex(".*/api/users/\\d+/schedules/calendar.*")) && method == "GET" -> {
                mockGetAllPetsMonthlySchedules()
            }

            // 반려동물별 월별 일정 조회
            path.matches(Regex(".*/api/users/\\d+/schedules/pets/\\d+/calendar.*")) && method == "GET" -> {
                mockGetPetMonthlySchedules()
            }

            // 반려동물별 일별 일정 조회
            path.matches(Regex(".*/api/pets/\\d+/schedules.*")) && method == "GET" -> {
                mockGetPetDailySchedules()
            }

            // 반려동물별 일정 생성
            path.matches(Regex(".*/api/pets/\\d+/schedules")) && method == "POST" -> {
                mockCreatePetSchedule()
            }

            // 알림 목록 조회
            path.matches(Regex(".*/api/pets/\\d+/alarms")) && method == "GET" -> {
                mockGetAlarms()
            }

            // 알림 ON/OFF 토글
            path.matches(Regex(".*/api/schedules/\\d+/alarm-status")) && method == "PATCH" -> {
                mockToggleAlarmStatus()
            }

            else -> {
                mockNotImplemented(path)
            }
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(mockResponse.code)
            .message(mockResponse.message)
            .body(mockResponse.body.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }

    // ==================== Mock 응답 생성 함수들 ====================

    /**
     * 소셜 로그인 Mock
     */
    private fun mockSocialLogin(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "AUTH_SUCCESS",
                    "message": "로그인 성공",
                    "result": {
                        "accessToken": "mock_access_token_${UUID.randomUUID()}",
                        "refreshToken": "mock_refresh_token_${UUID.randomUUID()}"
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 토큰 재발급 Mock
     */
    private fun mockTokenReissue(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "TOKEN_REISSUED",
                    "message": "토큰 재발급 성공",
                    "result": {
                        "accessToken": "mock_new_access_token_${UUID.randomUUID()}",
                        "refreshToken": "mock_new_refresh_token_${UUID.randomUUID()}"
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 프로필 조회 Mock
     */
    private fun mockGetProfile(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "PROFILE_SUCCESS",
                    "message": "프로필 조회 성공",
                    "result": {
                        "userId": "user_${UUID.randomUUID()}",
                        "userName": "테스트유저",
                        "userGender": "FEMALE",
                        "userBirthDate": "1995-05-15",
                        "userImageUrl": "https://example.com/profile.jpg",
                        "email": "test@example.com",
                        "alarmTime": "09:00",
                        "pets": [
                            {
                                "petId": "pet_${UUID.randomUUID()}",
                                "petName": "뭉치",
                                "petGender": "MALE",
                                "petBirthDate": "2020-03-10",
                                "petImageUrl": "https://example.com/pet.jpg",
                                "petType": "DOG",
                                "petBreed": "포메라니안",
                                "petIntroduction": "귀여운 우리 강아지"
                            }
                        ]
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 프로필 업데이트 Mock
     */
    private fun mockUpdateProfile(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "PROFILE_UPDATED",
                    "message": "프로필 업데이트 성공",
                    "result": {
                        "userId": "user_${UUID.randomUUID()}",
                        "userName": "업데이트된유저",
                        "petId": "pet_${UUID.randomUUID()}",
                        "petName": "업데이트된펫"
                    }
                }
            """.trimIndent()
        )
    }


    private fun mockCreateMomentPresignedUrls(): MockResponse {
        val itemsJson = (1..5).joinToString(",") {
            val key = "moments/${UUID.randomUUID()}.jpg"
            val url = "https://mock-s3-bucket.s3.amazonaws.com/$key?signature=mock"
            """{ "key": "$key", "url": "$url" }"""
        }

        return MockResponse(
            code = 201,
            message = "Created",
            body = """
        {
          "isSuccess": true,
          "code": "PRESIGNED_URL_CREATED",
          "message": "Presigned URL 발급 성공",
          "result": [
            $itemsJson
          ]
        }
        """.trimIndent()
        )
    }



    private fun mockCreateMomentByPet(): MockResponse {
        return MockResponse(

            code = 201,
            message = "Created",
            body = """
            {
              "isSuccess": true,
              "code": "MOMENT_CREATED",
              "message": "모먼트 생성 성공",
              "result": "moment_${UUID.randomUUID()}"
            }
        """.trimIndent()
        )
    }


    private fun mockGetMomentsByPet(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
        {
          "isSuccess": true,
          "code": "MOMENTS_SUCCESS",
          "message": "모먼트 조회 성공",
          "result": {
            "moments": [
              {
                "momentId": 1,
                "emotion": "HAPPINESS",
                "createdAt": "2026-02-10T14:30:00",
                "content": "오늘 산책하다가 친구 강아지를 만났어요!",
                "imageUrl": "https://picsum.photos/300/300"
              },
              {
                "momentId": 2,
                "emotion": "NEUTRAL",
                "createdAt": "2026-02-09T18:20:00",
                "content": "집에서 푹 쉬는 하루.",
                "imageUrl": "https://picsum.photos/300/300"
              },
              {
                "momentId": 3,
                "emotion": "SADNESS",
                "createdAt": "2026-02-08T09:15:00",
                "content": "병원 다녀오느라 조금 힘들었어요.",
                "imageUrl": "https://picsum.photos/300/300"
              }
            ],
            "pageInfo": {
              "page": 0,
              "size": 10,
              "totalPages": 1,
              "totalElements": 3,
              "hasNext": false,
              "hasPrevious": false
            }
          }
        }
        """.trimIndent()
        )
    }


    private fun mockGetMomentDetail(): MockResponse {
        val itemsJson = (1..3).joinToString(",") {
            val key = "moments/${UUID.randomUUID()}.jpg"
            """{ "imageKey": "$key" }"""
        }
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
        {
          "isSuccess": true,
          "code": "MOMENT_DETAIL_SUCCESS",
          "message": "모먼트 상세 조회 성공",
          "result": {
            "images": [ $itemsJson ],
            "emotion": "HAPPINESS",
            "content": "푸들이의 특별한 하루\n오늘은 정말 특별했어요. 산책도 하고 간식도 먹고..."
          }
        }
        """.trimIndent()
        )
    }



    /**
     * Presigned URL 발급 Mock
     */
    private fun mockCreatePresignedUrl(): MockResponse {
        val mockKey = "profile/${UUID.randomUUID()}.jpg"
        val mockUrl = "https://mock-s3-bucket.s3.amazonaws.com/$mockKey?signature=mock"

        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "PRESIGNED_URL_CREATED",
                    "message": "Presigned URL 발급 성공",
                    "result": {
                        "key": "$mockKey",
                        "url": "$mockUrl"
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 일정 조회 Mock
     */
    private fun mockGetSchedules(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "SCHEDULES_SUCCESS",
                    "message": "일정 조회 성공",
                    "result": {
                        "schedules": [
                            {
                                "scheduleId": "schedule_1",
                                "title": "병원 방문",
                                "date": "2026-02-15",
                                "time": "14:00",
                                "isCompleted": false
                            },
                            {
                                "scheduleId": "schedule_2",
                                "title": "산책",
                                "date": "2026-02-15",
                                "time": "18:00",
                                "isCompleted": true
                            }
                        ]
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 일정 생성 Mock
     */
    private fun mockCreateSchedule(): MockResponse {
        return MockResponse(
            code = 201,
            message = "Created",
            body = """
                {
                    "isSuccess": true,
                    "code": "SCHEDULE_CREATED",
                    "message": "일정 생성 성공",
                    "result": {
                        "scheduleId": "schedule_${UUID.randomUUID()}",
                        "title": "새로운 일정",
                        "date": "2026-02-20",
                        "time": "10:00"
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 오늘의 질문 조회 Mock
     */
    private fun mockGetTodayQuestion(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "QUESTION_SUCCESS",
                    "message": "오늘의 질문 조회 성공",
                    "result": {
                        "questionId": "q_${UUID.randomUUID()}",
                        "question": "오늘 반려동물과 함께한 가장 행복했던 순간은 무엇인가요?",
                        "date": "2026-02-12"
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 답변 제출 Mock
     */
    private fun mockSubmitAnswer(): MockResponse {
        return MockResponse(
            code = 201,
            message = "Created",
            body = """
                {
                    "isSuccess": true,
                    "code": "ANSWER_SUBMITTED",
                    "message": "답변 제출 성공",
                    "result": {
                        "answerId": "a_${UUID.randomUUID()}",
                        "questionId": "q_123",
                        "answer": "오늘 산책하면서 뭉치가 너무 즐거워했어요!"
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 질문 작성 여부 조회 Mock
     */
    private fun mockGetQuestionStatus(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "STATUS_SUCCESS",
                    "message": "작성 여부 조회 성공",
                    "result": {
                        "hasAnswered": false,
                        "date": "2026-02-12"
                    }
                }
            """.trimIndent()
        )
    }



    /**
     * 챗봇 메시지 전송 Mock
     */
    private fun mockSendChatMessage(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "MESSAGE_SENT",
                    "message": "메시지 전송 성공",
                    "result": {
                        "conversationId": "conv_${UUID.randomUUID()}",
                        "messageId": "msg_${UUID.randomUUID()}",
                        "response": "안녕하세요! 반려동물에 대해 어떤 고민이 있으신가요?",
                        "timestamp": "2026-02-12T10:30:00"
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 챗봇 대화 목록 조회 Mock
     */
    private fun mockGetConversations(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "CONVERSATIONS_SUCCESS",
                    "message": "대화 목록 조회 성공",
                    "result": {
                        "conversations": [
                            {
                                "conversationId": "conv_1",
                                "title": "강아지 사료 추천",
                                "lastMessage": "네, 도움이 되었다니 기쁩니다!",
                                "lastMessageTime": "2026-02-11T15:20:00"
                            },
                            {
                                "conversationId": "conv_2",
                                "title": "예방접종 시기",
                                "lastMessage": "생후 6주부터 시작하는 것이 좋습니다.",
                                "lastMessageTime": "2026-02-10T09:15:00"
                            }
                        ]
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 공지사항 조회 Mock
     */
    private fun mockGetNotices(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "NOTICES_SUCCESS",
                    "message": "공지사항 조회 성공",
                    "result": {
                        "notices": [
                            {
                                "noticeId": "n_1",
                                "title": "앱 업데이트 안내",
                                "content": "새로운 기능이 추가되었습니다.",
                                "date": "2026-02-10"
                            },
                            {
                                "noticeId": "n_2",
                                "title": "서비스 점검 안내",
                                "content": "2월 15일 새벽 2시~4시 서비스 점검이 있을 예정입니다.",
                                "date": "2026-02-08"
                            }
                        ]
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 반려동물 목록 조회 Mock
     */
    private fun mockGetPets(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "PETS_SUCCESS",
                    "message": "반려동물 조회 성공",
                    "result": [
                        {
                            "petId": "pet_001",
                            "petName": "뭉치",
                            "petImageKey": "pets/mungchi.jpg",
                            "petType": "DOG",
                            "petBreed": "포메라니안",
                            "petGender": "MALE",
                            "petBirthDate": "2020-03-10",
                            "petIntroduction": "귀여운 우리 강아지"
                        },
                        {
                            "petId": "pet_002",
                            "petName": "나비",
                            "petImageKey": "pets/nabi.jpg",
                            "petType": "CAT",
                            "petBreed": "코리안숏헤어",
                            "petGender": "FEMALE",
                            "petBirthDate": "2019-07-22",
                            "petIntroduction": "도도한 우리 고양이"
                        },
                        {
                            "petId": "pet_003",
                            "petName": "초코",
                            "petImageKey": "pets/choco.jpg",
                            "petType": "DOG",
                            "petBreed": "골든리트리버",
                            "petGender": "MALE",
                            "petBirthDate": "2021-01-15",
                            "petIntroduction": "순한 우리 대형견"
                        }
                    ]
                }
            """.trimIndent()
        )
    }

    /**
     * 사용자의 반려동물 목록 조회 (일정 관리용) Mock
     * SharedPreferences에서 실제 저장된 반려동물 정보 반환
     */
    private fun mockGetUserPets(): MockResponse {
        val prefs = getPrefs()

        if (prefs == null) {
            Log.w("MockApi", "SharedPreferences not available")
            return createEmptyPetsResponse()
        }

        try {
            val petsList = mutableListOf<Map<String, Any>>()

            // 첫 번째 반려동물 (기본)
            val petName = prefs.getString("pet_name", null)
            val petImageKey = prefs.getString("pet_profile_image_key", null)

            if (!petName.isNullOrEmpty()) {
                petsList.add(mapOf(
                    "petId" to 1L,
                    "profile" to (petImageKey ?: "")
                ))
                Log.d("MockApi", "Found pet: $petName")
            }

            // 추가 반려동물 (pet_info_1, pet_info_2, ...)
            val petIndex = prefs.getInt("pet_index", 0)
            if (petIndex > 0) {
                for (i in 1..petIndex) {
                    val petInfoJson = prefs.getString("pet_info_$i", null)
                    if (!petInfoJson.isNullOrEmpty()) {
                        try {
                            val petInfo = gson.fromJson(petInfoJson, Map::class.java)
                            val imageKey = petInfo["imageKey"] as? String ?: ""
                            petsList.add(mapOf(
                                "petId" to (i + 1L),
                                "profile" to imageKey
                            ))
                            Log.d("MockApi", "Found additional pet $i")
                        } catch (e: Exception) {
                            Log.e("MockApi", "Error parsing pet_info_$i", e)
                        }
                    }
                }
            }

            if (petsList.isEmpty()) {
                Log.d("MockApi", "No pets found in SharedPreferences")
                return createEmptyPetsResponse()
            }

            // API 응답 형식으로 변환
            val petsArray = petsList.joinToString(",\n") { pet ->
                """
                        {
                            "petId": ${pet["petId"]},
                            "profile": "${pet["profile"]}"
                        }
                """.trimIndent()
            }

            Log.d("MockApi", "Returning ${petsList.size} pets from local storage")

            return MockResponse(
                code = 200,
                message = "OK",
                body = """
                    {
                        "pets": [
                            $petsArray
                        ]
                    }
                """.trimIndent()
            )
        } catch (e: Exception) {
            Log.e("MockApi", "Error reading pets from SharedPreferences", e)
            return createEmptyPetsResponse()
        }
    }

    /**
     * 반려동물이 없을 때 빈 응답 반환
     */
    private fun createEmptyPetsResponse(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "pets": []
                }
            """.trimIndent()
        )
    }

    /**
     * 모든 반려동물 월별 일정 조회 Mock
     * SharedPreferences에서 실제 저장된 일정 반환
     */
    private fun mockGetAllPetsMonthlySchedules(): MockResponse {
        val prefs = getPrefs()

        if (prefs == null) {
            return createEmptyMonthlySchedulesResponse()
        }

        try {
            val schedulesJson = prefs.getString("schedules_list", null)

            if (schedulesJson.isNullOrEmpty()) {
                return createEmptyMonthlySchedulesResponse()
            }

            val type = object : TypeToken<List<LocalSchedule>>() {}.type
            val schedules: List<LocalSchedule> = gson.fromJson(schedulesJson, type)

            // 날짜별로 일정 개수 집계
            val dateCountMap = mutableMapOf<String, Int>()

            schedules.forEach { schedule ->
                if (schedule.isOneTime && schedule.date != null) {
                    // 일회성 일정
                    dateCountMap[schedule.date] = (dateCountMap[schedule.date] ?: 0) + 1
                } else if (!schedule.isOneTime && schedule.repeatDays != null) {
                    // 반복 일정 - 현재 월의 해당 요일들에 추가
                    // 간단하게 처리: 반복 일정이 있으면 해당 요일마다 카운트
                    // 실제로는 현재 월의 모든 해당 요일을 계산해야 함
                }
            }

            val daysArray = dateCountMap.entries.joinToString(",\n") { (date, count) ->
                """
                        {
                            "date": "$date",
                            "count": $count
                        }
                """.trimIndent()
            }

            return MockResponse(
                code = 200,
                message = "OK",
                body = """
                    {
                        "year": 2026,
                        "month": 2,
                        "days": [
                            $daysArray
                        ]
                    }
                """.trimIndent()
            )
        } catch (e: Exception) {
            Log.e("MockApi", "Error getting monthly schedules", e)
            return createEmptyMonthlySchedulesResponse()
        }
    }

    private fun createEmptyMonthlySchedulesResponse(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "year": 2026,
                    "month": 2,
                    "days": []
                }
            """.trimIndent()
        )
    }

    /**
     * 반려동물별 월별 일정 조회 Mock
     */
    private fun mockGetPetMonthlySchedules(): MockResponse {
        val prefs = getPrefs()

        if (prefs == null) {
            return createEmptyPetMonthlySchedulesResponse()
        }

        try {
            // URL에서 petId 추출
            // 실제로는 request.url에서 추출해야 하지만 간단하게 처리

            val schedulesJson = prefs.getString("schedules_list", null)

            if (schedulesJson.isNullOrEmpty()) {
                return createEmptyPetMonthlySchedulesResponse()
            }

            val type = object : TypeToken<List<LocalSchedule>>() {}.type
            val schedules: List<LocalSchedule> = gson.fromJson(schedulesJson, type)

            // 특정 반려동물의 일정만 필터링 (간단하게 모든 일정 반환)
            val dateCountMap = mutableMapOf<String, Int>()

            schedules.forEach { schedule ->
                if (schedule.isOneTime && schedule.date != null) {
                    dateCountMap[schedule.date] = (dateCountMap[schedule.date] ?: 0) + 1
                }
            }

            val daysArray = dateCountMap.entries.joinToString(",\n") { (date, count) ->
                """
                        {
                            "date": "$date",
                            "count": $count
                        }
                """.trimIndent()
            }

            return MockResponse(
                code = 200,
                message = "OK",
                body = """
                    {
                        "petId": 1,
                        "year": 2026,
                        "month": 2,
                        "days": [
                            $daysArray
                        ]
                    }
                """.trimIndent()
            )
        } catch (e: Exception) {
            Log.e("MockApi", "Error getting pet monthly schedules", e)
            return createEmptyPetMonthlySchedulesResponse()
        }
    }

    private fun createEmptyPetMonthlySchedulesResponse(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "petId": 1,
                    "year": 2026,
                    "month": 2,
                    "days": []
                }
            """.trimIndent()
        )
    }

    /**
     * 반려동물별 일별 일정 조회 Mock
     */
    private fun mockGetPetDailySchedules(): MockResponse {
        val prefs = getPrefs()

        if (prefs == null) {
            return createEmptyDailySchedulesResponse()
        }

        try {
            val schedulesJson = prefs.getString("schedules_list", null)

            if (schedulesJson.isNullOrEmpty()) {
                return createEmptyDailySchedulesResponse()
            }

            val type = object : TypeToken<List<LocalSchedule>>() {}.type
            val schedules: List<LocalSchedule> = gson.fromJson(schedulesJson, type)

            // 오늘 날짜의 일정만 필터링
            val todaySchedules = schedules.filter { schedule ->
                // 일회성 일정이거나, 반복 일정인 경우
                schedule.isOneTime || !schedule.repeatDays.isNullOrEmpty()
            }

            val schedulesArray = todaySchedules.joinToString(",\n") { schedule ->
                val startAt = if (schedule.date != null) {
                    "${schedule.date}T${schedule.time}:00"
                } else {
                    "2026-02-17T${schedule.time}:00"
                }

                """
                        {
                            "scheduleId": ${schedule.scheduleId},
                            "title": "${schedule.title}",
                            "startAt": "$startAt",
                            "memo": ${if (schedule.memo != null) "\"${schedule.memo}\"" else "null"},
                            "category": "${schedule.category}",
                            "durationMinutes": ${schedule.durationMinutes ?: 0},
                            "isAlarmEnabled": ${schedule.isAlarmEnabled}
                        }
                """.trimIndent()
            }

            return MockResponse(
                code = 200,
                message = "OK",
                body = """
                    {
                        "petId": 1,
                        "date": "2026-02-17",
                        "schedules": [
                            $schedulesArray
                        ]
                    }
                """.trimIndent()
            )
        } catch (e: Exception) {
            Log.e("MockApi", "Error getting daily schedules", e)
            return createEmptyDailySchedulesResponse()
        }
    }

    private fun createEmptyDailySchedulesResponse(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "petId": 1,
                    "date": "2026-02-17",
                    "schedules": []
                }
            """.trimIndent()
        )
    }

    /**
     * 반려동물별 일정 생성 Mock
     * SharedPreferences에 실제로 저장
     */
    private fun mockCreatePetSchedule(): MockResponse {
        val prefs = getPrefs()

        try {
            // 새로운 scheduleId 생성
            val newScheduleId = System.currentTimeMillis()

            // 기존 일정 목록 가져오기
            if (prefs != null) {
                val schedulesJson = prefs.getString("schedules_list", null)
                val schedules = if (schedulesJson.isNullOrEmpty()) {
                    mutableListOf<LocalSchedule>()
                } else {
                    val type = object : TypeToken<MutableList<LocalSchedule>>() {}.type
                    gson.fromJson(schedulesJson, type)
                }

                // TODO: 실제 요청 바디에서 일정 정보 파싱하여 추가
                // 지금은 기본 일정만 추가

                // 변경된 목록 저장
                val updatedJson = gson.toJson(schedules)
                prefs.edit().putString("schedules_list", updatedJson).apply()

                Log.d("MockApi", "Schedule created with id: $newScheduleId")
            }

            return MockResponse(
                code = 200,
                message = "OK",
                body = """
                    {
                        "scheduleId": "$newScheduleId",
                        "title": "새로운 일정"
                    }
                """.trimIndent()
            )
        } catch (e: Exception) {
            Log.e("MockApi", "Error creating schedule", e)
            return MockResponse(
                code = 500,
                message = "Internal Server Error",
                body = """
                    {
                        "isSuccess": false,
                        "message": "일정 생성 실패"
                    }
                """.trimIndent()
            )
        }
    }

    /**
     * 알림 목록 조회 Mock
     * SharedPreferences에서 저장된 일정을 알림 형태로 반환
     */
    private fun mockGetAlarms(): MockResponse {
        val prefs = getPrefs()

        if (prefs == null) {
            return createEmptyAlarmsResponse()
        }

        try {
            val schedulesJson = prefs.getString("schedules_list", null)

            if (schedulesJson.isNullOrEmpty()) {
                return createEmptyAlarmsResponse()
            }

            val type = object : TypeToken<List<LocalSchedule>>() {}.type
            val schedules: List<LocalSchedule> = gson.fromJson(schedulesJson, type)

            // 알림이 활성화된 일정만 반환
            val alarms = schedules.filter { it.isAlarmEnabled }

            val alarmsArray = alarms.joinToString(",\n") { schedule ->
                val repeatDaysJson = if (schedule.repeatDays != null) {
                    schedule.repeatDays.joinToString("\", \"", prefix = "[\"", postfix = "\"]")
                } else {
                    "null"
                }

                val dateJson = if (schedule.date != null) "\"${schedule.date}\"" else "null"

                """
                        {
                            "scheduleId": ${schedule.scheduleId},
                            "title": "${schedule.title}",
                            "category": "${schedule.category}",
                            "repeatDays": $repeatDaysJson,
                            "date": $dateJson,
                            "alarmTime": "${schedule.time}:00",
                            "durationMinutes": ${schedule.durationMinutes ?: 0},
                            "isOneTime": ${schedule.isOneTime},
                            "isAlarmEnabled": ${schedule.isAlarmEnabled}
                        }
                """.trimIndent()
            }

            Log.d("MockApi", "Returning ${alarms.size} alarms from local storage")

            return MockResponse(
                code = 200,
                message = "OK",
                body = """
                    {
                        "isSuccess": true,
                        "code": "ALARM_SUCCESS",
                        "message": "알림 목록 조회 성공",
                        "result": {
                            "petId": 1,
                            "alarms": [
                                $alarmsArray
                            ]
                        }
                    }
                """.trimIndent()
            )
        } catch (e: Exception) {
            Log.e("MockApi", "Error getting alarms", e)
            return createEmptyAlarmsResponse()
        }
    }

    private fun createEmptyAlarmsResponse(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "ALARM_SUCCESS",
                    "message": "알림 목록 조회 성공",
                    "result": {
                        "petId": 1,
                        "alarms": []
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 알림 ON/OFF 토글 Mock
     * SharedPreferences에 실제로 반영
     */
    private fun mockToggleAlarmStatus(): MockResponse {
        val prefs = getPrefs()

        if (prefs != null) {
            try {
                // TODO: 실제 요청에서 scheduleId와 isEnabled 값 파싱
                // 해당 일정의 isAlarmEnabled 값 업데이트

                val schedulesJson = prefs.getString("schedules_list", null)

                if (!schedulesJson.isNullOrEmpty()) {
                    val type = object : TypeToken<MutableList<LocalSchedule>>() {}.type
                    val schedules: MutableList<LocalSchedule> = gson.fromJson(schedulesJson, type)

                    // 변경된 목록 저장
                    val updatedJson = gson.toJson(schedules)
                    prefs.edit().putString("schedules_list", updatedJson).apply()

                    Log.d("MockApi", "Alarm status toggled")
                }
            } catch (e: Exception) {
                Log.e("MockApi", "Error toggling alarm status", e)
            }
        }

        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "ALARM_TOGGLED",
                    "message": "알림 상태 변경 성공"
                }
            """.trimIndent()
        )
    }

    /**
     * 구현되지 않은 API Mock
     */
    private fun mockNotImplemented(path: String): MockResponse {
        return MockResponse(
            code = 501,
            message = "Not Implemented",
            body = """
                {
                    "isSuccess": false,
                    "code": "NOT_IMPLEMENTED",
                    "message": "Mock API가 아직 구현되지 않았습니다: $path"
                }
            """.trimIndent()
        )
    }

    /**
     * Mock 응답 데이터 클래스
     */
    private data class MockResponse(
        val code: Int,
        val message: String,
        val body: String
    )
}

/**
 * SharedPreferences에 저장된 반려동물 데이터 모델
 */
data class LocalPet(
    val petId: Long,
    val petName: String? = null,
    val petImageUrl: String? = null,
    val petType: String? = null,
    val petBreed: String? = null,
    val petGender: String? = null,
    val petBirthDate: String? = null,
    val petIntroduction: String? = null
)

/**
 * SharedPreferences에 저장된 일정 데이터 모델
 */
data class LocalSchedule(
    val scheduleId: Long,
    val petId: Long,
    val title: String,
    val category: String,
    val date: String? = null,         // "YYYY-MM-DD" 일회성 일정
    val repeatDays: List<String>? = null,  // ["MONDAY", "WEDNESDAY"] 반복 일정
    val time: String,                 // "HH:mm"
    val durationMinutes: Int? = null,
    val memo: String? = null,
    val isAlarmEnabled: Boolean = true,
    val isOneTime: Boolean = true
)

/**
 * SharedPreferences에 저장된 사용자 데이터 모델
 */
data class LocalUser(
    val userId: Long,
    val userName: String? = null,
    val userEmail: String? = null
)