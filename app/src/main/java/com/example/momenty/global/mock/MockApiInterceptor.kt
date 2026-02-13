package com.example.momenty.global.mock

import android.content.Context
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.UUID

/**
 * Mock API Interceptor
 * 백엔드 없이 테스트하기 위한 가짜 API 응답 생성
 *
 * 사용법:
 * 1. NetworkModule에서 이 Interceptor를 OkHttpClient에 추가
 * 2. MockApiInterceptor.isMockEnabled = true/false로 Mock 모드 제어
 */
class MockApiInterceptor(private val context: Context) : Interceptor {

    companion object {
        // Mock 모드 활성화 여부 (build.gradle에서 설정)
        var isMockEnabled = false

        // Mock 지연 시간 (ms)
        private const val MOCK_DELAY = 1000L
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
            path.endsWith("/api/moments") && method == "GET" -> {
                mockGetMoments()
            }
            path.endsWith("/api/moments") && method == "POST" -> {
                mockCreateMoment()
            }
            path.matches(Regex(".*/api/moments/\\d+")) && method == "GET" -> {
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
     * 모먼트 리스트 조회 Mock
     */
    private fun mockGetMoments(): MockResponse {
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
                                "momentId": "m_1",
                                "title": "행복한 산책",
                                "emotion": "HAPPY",
                                "imageUrl": "https://example.com/moment1.jpg",
                                "date": "2026-02-10"
                            },
                            {
                                "momentId": "m_2",
                                "title": "병원 다녀옴",
                                "emotion": "WORRIED",
                                "imageUrl": "https://example.com/moment2.jpg",
                                "date": "2026-02-09"
                            }
                        ]
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 모먼트 생성 Mock
     */
    private fun mockCreateMoment(): MockResponse {
        return MockResponse(
            code = 201,
            message = "Created",
            body = """
                {
                    "isSuccess": true,
                    "code": "MOMENT_CREATED",
                    "message": "모먼트 생성 성공",
                    "result": {
                        "momentId": "m_${UUID.randomUUID()}",
                        "title": "새로운 모먼트",
                        "emotion": "HAPPY"
                    }
                }
            """.trimIndent()
        )
    }

    /**
     * 모먼트 상세 조회 Mock
     */
    private fun mockGetMomentDetail(): MockResponse {
        return MockResponse(
            code = 200,
            message = "OK",
            body = """
                {
                    "isSuccess": true,
                    "code": "MOMENT_DETAIL_SUCCESS",
                    "message": "모먼트 상세 조회 성공",
                    "result": {
                        "momentId": "m_1",
                        "title": "행복한 산책",
                        "content": "오늘 공원에서 다른 강아지 친구들을 많이 만났어요!",
                        "emotion": "HAPPY",
                        "imageUrl": "https://example.com/moment1.jpg",
                        "date": "2026-02-10",
                        "createdAt": "2026-02-10T14:30:00"
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
     * SharedPreferences에 저장된 실제 반려동물 데이터를 반환
     */
    private fun mockGetPets(): MockResponse {
        val prefs = context.getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)

        // SharedPreferences에서 반려동물 정보 읽기
        val petName = prefs.getString("pet_name", null)
        val isPetRegistered = prefs.getBoolean("pet_profile_completed", false)

        return if (isPetRegistered && petName != null) {
            // 저장된 반려동물이 있는 경우 - 이름만 포함
            MockResponse(
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
                                "petName": "$petName",
                                "petImageKey": "",
                                "petType": "DOG",
                                "petBreed": "",
                                "petGender": "MALE",
                                "petBirthDate": "2020-01-01",
                                "petIntroduction": ""
                            }
                        ]
                    }
                """.trimIndent()
            )
        } else {
            // 저장된 반려동물이 없는 경우 빈 배열 반환
            MockResponse(
                code = 200,
                message = "OK",
                body = """
                    {
                        "isSuccess": true,
                        "code": "PETS_SUCCESS",
                        "message": "반려동물 조회 성공",
                        "result": []
                    }
                """.trimIndent()
            )
        }
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