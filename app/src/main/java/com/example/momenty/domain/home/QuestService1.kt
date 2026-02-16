package com.example.momenty.domain.home

import android.util.Log
import com.example.momenty.di.NetworkModule.getRetrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuestService1 {
    /*
    private lateinit var loadQuestView: LoadQuestView
    private lateinit var writeQuestView: WriteQuestView

    fun setLoadQuestView(loadQuestView: LoadQuestView) {
        this.loadQuestView = loadQuestView
    }
    fun setWriteQuestView(writeQuestView: WriteQuestView) {
        this.writeQuestView = writeQuestView
    }

    fun loadQuestAPI(token: String) {
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

        val questService = getRetrofit().create(QuestService::class.java)
        questService.loadQuest(bearerToken).enqueue(object: Callback<LoadQuestResponse> {
            override fun onResponse(
                call: Call<LoadQuestResponse?>,
                response: Response<LoadQuestResponse?>
            ) {
                val resp: LoadQuestResponse = response.body()!!

                if(::loadQuestView.isInitialized) {
                    when(resp.code) {
                        "DAILY200-1" -> loadQuestView.onLoadQuestSuccess(resp.result!!)
                        "DAILY404-1" -> Log.d("LoadQuest", "에러")
                        "COMMON401-1" -> Log.d("LoadQuest", "인증 실패")
                        "COMMON500-1" -> Log.d("LoadQuest", "서버 에러")
                        else -> loadQuestView.onLoadQuestFailure()
                    }
                }
            }

            override fun onFailure(call: Call<LoadQuestResponse?>, t: Throwable) {
                Log.d("LoadQuest", "그냥 실패", t)
            }
        })
    }

    fun writeQuestAPI(writeQuestRequest: WriteQuestRequest) {
        val questService = getRetrofit().create(QuestService::class.java)
        questService.writeQuest(writeQuestRequest).enqueue(object: Callback<WriteQuestResponse> {
            override fun onResponse(
                call: Call<WriteQuestResponse?>,
                response: Response<WriteQuestResponse?>
            ) {
                val resp: WriteQuestResponse = response.body()!!

                if (::writeQuestView.isInitialized) {
                    Log.d("WriteQuest", "호출 성공")
                    when(resp.code) {
                        "DAILY201-1" -> writeQuestView.onWriteQuestSuccess()
                        "DAILY403-1" -> Log.d("WriteQuest", "다른 사람 질문 답변")
                        "COMMON401-1" -> Log.d("WriteQuest", "인증 실패")
                        "COMMON500-1" -> Log.d("WriteQuest", "서버 에러")
                        "DAILY404-1" -> Log.d("WriteQuest", "질문id 해당하는 질문 없음")
                        else -> writeQuestView.onWriteQuestFailure()
                    }
                }
            }

            override fun onFailure(call: Call<WriteQuestResponse?>, t: Throwable) {
                Log.d("WriteQuest", "그냥 실패", t)
            }
        })
    }

     */
}