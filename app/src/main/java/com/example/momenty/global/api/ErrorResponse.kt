package com.example.momenty.global.api

/**
 * API 에러 응답 처리
 */
data class ErrorResponse(
    val code: String,         // 에러 코드 (예: "MEMBER4001")
    val message: String,      // 에러 메시지
    val timestamp: String? = null
)

/**
 * API 에러 예외 클래스
 */
class ApiException(
    val code: String,
    override val message: String
) : Exception(message)