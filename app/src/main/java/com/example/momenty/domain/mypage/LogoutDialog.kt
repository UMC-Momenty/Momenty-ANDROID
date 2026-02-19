package com.example.momenty.domain.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.momenty.databinding.DialogMyPageLogoutBinding
import com.example.momenty.databinding.DialogNotificationConfirmBinding
import com.example.momenty.domain.mypage.data.NotificationData

class LogoutDialog(myInterface: MyLogoutInterface): DialogFragment() {
    private var _binding: DialogMyPageLogoutBinding ?= null
    private val binding get() = _binding!!

    private var myInterface: MyLogoutInterface ?= null

    init {
        this.myInterface = myInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogMyPageLogoutBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        binding.btnDialogCancel.setOnClickListener {
            dismiss()
        }
        binding.btnDialogLogout.setOnClickListener {
            this.myInterface?.onLogoutClickListener()
            dismiss()
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

interface MyLogoutInterface{
    fun onLogoutClickListener()
}