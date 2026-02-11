package com.example.momenty.domain.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentNotificationBinding

class NotificationFragment: Fragment() {
    lateinit var binding: FragmentNotificationBinding

    private var notificationDatas = ArrayList<NotificationData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)

        inputDummyData()
        setRVA()
        initListener()

        return binding.root
    }

    private fun initListener() {
        binding.btnNotificationBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun inputDummyData() {
        notificationDatas.apply {
            clear()
            add(NotificationData(
                "서비스 이용 안내",
                "ON"))

            add(NotificationData(
                "마케팅 정보 알림",
                "OFF"))
        }
    }

    private fun setRVA() {
        val RVAdapter = NotificationRVA(notificationDatas)
        binding.rvNotification.adapter = RVAdapter
        binding.rvNotification.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )

        RVAdapter.setMyItemClickListener(object: NotificationRVA.MyItemClickListener{
            override fun onItemClick(history: NotificationData) {
                // TODO("Not yet implemented")
            }
        })
    }
}