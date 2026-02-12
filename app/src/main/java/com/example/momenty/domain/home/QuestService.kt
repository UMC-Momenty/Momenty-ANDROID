package com.example.momenty.domain.home

import android.util.Log
import com.example.momenty.di.NetworkModule.getRetrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuestService {
    private lateinit var loadQuestView: LoadQuestView
    private lateinit var writeQuestView: WriteQuestView

    fun setLoadQuestView(loadQuestView: LoadQuestView) {
        this.loadQuestView = loadQuestView
    }
    fun setWriteQuestView(writeQuestView: WriteQuestView) {
        this.writeQuestView = writeQuestView
    }

    fun loadQuest(token: String) {
        val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"

        val questService = getRetrofit().create(QuestApi::class.java)
        questService.loadQuest(bearerToken).enqueue(object: Callback<LoadQuestResponse> {
            override fun onResponse(
                call: Call<LoadQuestResponse?>,
                response: Response<LoadQuestResponse?>
            ) {
                val resp: LoadQuestResponse = response.body()!!

                if(::loadQuestView.isInitialized) {
                    when(resp.code) {
                        "DAILY200-1" -> loadQuestView.onLoadQuestSuccess(resp.result!!)
                        else -> loadQuestView.onLoadQuestFailure()
                    }
                }
            }

            override fun onFailure(call: Call<LoadQuestResponse?>, t: Throwable) {
                Log.d("LoadQuest/Failure", t.message.toString())
            }
        })
    }

    fun writeQuest(writeQuestRequest: WriteQuestRequest) {
        val questService = getRetrofit().create(QuestApi::class.java)
        questService.writeQuest(writeQuestRequest).enqueue(object: Callback<WriteQuestResponse> {
            override fun onResponse(
                call: Call<WriteQuestResponse?>,
                response: Response<WriteQuestResponse?>
            ) {
                val resp: WriteQuestResponse = response.body()!!

                if (::writeQuestView.isInitialized) {
                    when(resp.code) {
                        "DAILY201-1" -> writeQuestView.onWriteQuestSuccess()
                        else -> writeQuestView.onWriteQuestFailure()
                    }
                }
            }

            override fun onFailure(call: Call<WriteQuestResponse?>, t: Throwable) {
                Log.d("WriteQuest/Failure", t.message.toString())
            }
        })
    }
}