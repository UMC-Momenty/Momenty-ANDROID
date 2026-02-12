package com.example.momenty.domain.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentNotificationBinding
import com.example.momenty.domain.home.ConfirmDialogInterface
import com.example.momenty.domain.home.WriteQuestionDialog

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
//
//        binding.btnWriteQuestionPetFormSave.setOnClickListener {
//            val confirmDialog = WriteQuestionDialog(this, "", 0)
//            confirmDialog.show(this.supportFragmentManager, "ConfirmDialog")
//        }
    }

    private fun inputDummyData() {
        notificationDatas.apply {
            clear()
            add(NotificationData(
                "알림",
                "ON",
                "알림을 받으시겠어요?"))

            add(NotificationData(
                "마케팅 정보 알림",
                "OFF",
                "마케팅 정보 알림을 받으시겠어요?"))
        }
    }

    private fun setRVA() {
        val RVAdapter = NotificationRVA(notificationDatas)
        binding.rvNotification.adapter = RVAdapter
        binding.rvNotification.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )

    }

}