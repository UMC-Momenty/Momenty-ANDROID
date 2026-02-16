package com.example.momenty.global.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun String.toKoreanDateOrFallback(): String {
    val seoulTz = TimeZone.getTimeZone("Asia/Seoul")


    val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", // 2026-02-17T01:23:45.123+09:00
        "yyyy-MM-dd'T'HH:mm:ssXXX",     // 2026-02-17T01:23:45+09:00
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",   // 2026-02-17T01:23:45.123Z or +0900
        "yyyy-MM-dd'T'HH:mm:ssX"        // 2026-02-17T01:23:45Z or +0900
    )

    val date = inputPatterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }.parse(this)
        }.getOrNull()
    } ?: return this

    val out = SimpleDateFormat("yyyy.MM.dd (E) a h:mm", Locale.KOREAN).apply {
        timeZone = seoulTz
    }
    return out.format(date)
}
