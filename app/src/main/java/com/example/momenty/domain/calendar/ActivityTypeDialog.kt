package com.example.momenty.domain.calendar

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import com.example.momenty.R
import com.example.momenty.databinding.DialogActivityTypeSelectBinding

class ActivityTypeDialog(
    context: Context,
    private val onActivityTypeSelected: (String) -> Unit,
) : Dialog(context) {

    private lateinit var binding: DialogActivityTypeSelectBinding
    private var selectedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogActivityTypeSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 다이얼로그 배경 투명하게 설정
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        setupButtons()
    }

    private fun setupButtons() {
        val activityButtons = listOf(
            binding.btnActivityWalk to R.string.type_walk,
            binding.btnActivityEat to R.string.type_eat,
            binding.btnActivityBeauty to R.string.type_beauty,
            binding.btnActivityHealth to R.string.type_health,
            binding.btnActivityMedician to R.string.type_medician,
            binding.btnActivityTreat to R.string.type_treat,
            binding.btnActivityEtc to R.string.type_etc
        )

        activityButtons.forEach { (button, stringResId) ->
            button.setOnClickListener {
                val activityType = context.getString(stringResId)
                onActivityTypeSelected(activityType)
                dismiss()
            }
        }
    }
}