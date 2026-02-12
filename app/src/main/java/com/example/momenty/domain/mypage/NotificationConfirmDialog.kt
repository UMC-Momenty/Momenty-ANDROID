package com.example.momenty.domain.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.momenty.databinding.DialogNotificationConfirmBinding
import com.example.momenty.databinding.DialogQuestSaveBinding
import com.example.momenty.domain.mypage.NotificationData

class NotificationConfirmDialog(myInterface: MyNotifyInterface,
                          data: NotificationData, id: Int): DialogFragment() {
    private var _binding: DialogNotificationConfirmBinding?= null
    private val binding get() = _binding!!

    private var myInterface: MyNotifyInterface ?= null
    private var text: String ?= null
    private var id: Int ?= null

    init {
        this.text = data.content
        this.id = id
        this.myInterface = myInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogNotificationConfirmBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        binding.tvDialogContent.text = text
        binding.btnDialogCancel.setOnClickListener {
            this.myInterface?.onCancelClickListener(id!!)
            dismiss()
        }
        binding.btnDialogSave.setOnClickListener {
            this.myInterface?.onSaveClickListener(id!!)
            dismiss()
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

interface MyNotifyInterface{
    fun onSaveClickListener(id: Int)
    fun onCancelClickListener(id: Int)
}