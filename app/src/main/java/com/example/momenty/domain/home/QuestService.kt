//package com.example.momenty.domain.home
//
//import android.util.Log
//import com.example.momenty.di.NetworkModule
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class QuestService {
//
//    private lateinit var loadQuestView: LoadQuestView
//    private lateinit var writeQuestView: WriteQuestView
//
//    fun setLoadQuestView(loadQuestView: LoadQuestView) {
//        this.loadQuestView = loadQuestView
//    }
//
//    fun setWriteQuestView(writeQuestView: WriteQuestView) {
//        this.writeQuestView = writeQuestView
//    }
//
//    fun loadQuest(token: String) {
//        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
//
//        val questApi = NetworkModule.getRetrofit().create(QuestApi::class.java)
//        questApi.loadQuest(bearerToken).enqueue(object : Callback<LoadQuestResponse> {
//            override fun onResponse(
//                call: Call<LoadQuestResponse>,
//                response: Response<LoadQuestResponse>
//            ) {
//                val resp = response.body()
//                if (resp == null) {
//                    if (::loadQuestView.isInitialized) loadQuestView.onLoadQuestFailure()
//                    return
//                }
//
//                if (::loadQuestView.isInitialized) {
//                    when (resp.code) {
//                        "DAILY200-1" -> loadQuestView.onLoadQuestSuccess(resp.result!!)
//                        else -> loadQuestView.onLoadQuestFailure()
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<LoadQuestResponse>, t: Throwable) {
//                Log.d("API", "호출 실패", t)
//                if (::loadQuestView.isInitialized) loadQuestView.onLoadQuestFailure()
//            }
//        })
//    }
//
//    fun writeQuest(writeQuestRequest: WriteQuestRequest) {
//        val questApi = NetworkModule.getRetrofit().create(QuestApi::class.java)
//        questApi.writeQuest(writeQuestRequest).enqueue(object : Callback<WriteQuestResponse> {
//            override fun onResponse(
//                call: Call<WriteQuestResponse>,
//                response: Response<WriteQuestResponse>
//            ) {
//                val resp = response.body()
//                if (resp == null) {
//                    if (::writeQuestView.isInitialized) writeQuestView.onWriteQuestFailure()
//                    return
//                }
//
//                if (::writeQuestView.isInitialized) {
//                    when (resp.code) {
//                        "DAILY201-1" -> writeQuestView.onWriteQuestSuccess()
//                        else -> writeQuestView.onWriteQuestFailure()
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<WriteQuestResponse>, t: Throwable) {
//                Log.d("WriteQuest/Failure", t.message.toString())
//                if (::writeQuestView.isInitialized) writeQuestView.onWriteQuestFailure()
//            }
//        })
//    }
//}
