package com.example.momenty.global.api

/**
 * 백엔드 공통 응답 형식
 * 모든 API 응답이 이 형식을 따름
 */
data class BaseResponse<T>(
    val isSuccess: Boolean,      // 성공 여부
    val code: String,             // 응답 코드 (예: "MEMBER200")
    val message: String,          // 응답 메시지
    val result: T?                // 실제 데이터 (제네릭)
)