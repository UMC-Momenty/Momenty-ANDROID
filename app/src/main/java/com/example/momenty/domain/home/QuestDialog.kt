package com.example.momenty.domain.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.momenty.databinding.DialogQuestSaveBinding

class WriteQuestionDialog(confirmDialogInterface: ConfirmDialogInterface,
                          text: String, id: Int): DialogFragment() {
    private var _binding: DialogQuestSaveBinding?= null
    private val binding get() = _binding!!

    private var confirmDialogInterface: ConfirmDialogInterface ?= null
    private var text: String ?= null
    private var id: Int ?= null

    init {
        this.text = text
        this.id = id
        this.confirmDialogInterface = confirmDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogQuestSaveBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnDialogCancel.setOnClickListener {
            dismiss()
        }
        binding.btnDialogSave.setOnClickListener {
            this.confirmDialogInterface?.onSaveClickListener(id!!)
            dismiss()
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

interface ConfirmDialogInterface{
    fun onSaveClickListener(id: Int)
}