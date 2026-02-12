package com.example.momenty.domain.calendar

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
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

                // '기타'를 선택한 경우 직접 입력 다이얼로그 표시
                if (stringResId == R.string.type_etc) {
                    showCustomInputDialog()
                } else {
                    onActivityTypeSelected(activityType)
                    dismiss()
                }
            }
        }
    }

    /**
     * '기타' 선택 시 직접 입력 다이얼로그
     */
    private fun showCustomInputDialog() {
        val editText = EditText(context).apply {
            hint = "활동 유형을 입력하세요"
            setPadding(40, 40, 40, 40)
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
            addView(editText)
        }

        AlertDialog.Builder(context)
            .setTitle("활동 유형 입력")
            .setView(layout)
            .setPositiveButton("확인") { dialog, _ ->
                val customType = editText.text.toString().trim()
                if (customType.isNotEmpty()) {
                    onActivityTypeSelected(customType)
                    dismiss()
                    dialog.dismiss()
                } else {
                    // 입력이 없으면 다시 입력 요청
                    editText.error = "활동 유형을 입력해주세요"
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}