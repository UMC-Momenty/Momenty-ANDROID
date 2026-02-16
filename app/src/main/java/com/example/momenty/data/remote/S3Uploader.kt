package com.example.momenty.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object S3Uploader {
    private val client = OkHttpClient()

    fun upload(presignedUrl: String, bytes: ByteArray, contentType: String) {
        val body = bytes.toRequestBody(contentType.toMediaType())

        val request = Request.Builder()
            .url(presignedUrl)
            .put(body)
            .addHeader("Content-Type", contentType)
            .build()

        client.newCall(request).execute().use { res ->
            if (!res.isSuccessful) {
                throw IllegalStateException("S3 업로드 실패 code=${res.code}")
            }
        }
    }
}
