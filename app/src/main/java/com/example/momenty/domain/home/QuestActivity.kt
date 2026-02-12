package com.example.momenty.domain.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.momenty.databinding.ActivityQuestBinding
import com.example.momenty.global.security.TokenManager
import javax.inject.Inject

class QuestActivity/* @Inject constructor(
    private val tokenManager: TokenManager
)*/: AppCompatActivity(), ConfirmDialogInterface, LoadQuestView, WriteQuestView {

    lateinit var binding: ActivityQuestBinding
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuestBinding.inflate(layoutInflater)
        tokenManager = TokenManager(this)

        setContentView(binding.root)

        initListener()
        textCount()
        loadQuest()

    }

    private fun initListener() {
        binding.btnWriteQuestionPetFormSave.setOnClickListener {
            val confirmDialog = WriteQuestionDialog(this, "", 0)
            confirmDialog.show(this.supportFragmentManager, "ConfirmDialog")
        }

        binding.btnWriteQuestionBookmarkOff.setOnClickListener {
            binding.btnWriteQuestionBookmarkOff.visibility = View.GONE
            binding.btnWriteQuestionBookmarkOn.visibility = View.VISIBLE
        }

        binding.btnWriteQuestionBookmarkOn.setOnClickListener {
            binding.btnWriteQuestionBookmarkOn.visibility = (View.GONE)
            binding.btnWriteQuestionBookmarkOff.setVisibility(View.VISIBLE)
        }

        binding.btnWriteQuestionBack.setOnClickListener {
            finish()
        }
    }
    private fun textCount() {
        val editText = binding.etWriteQuestionAnswer
        val countText = binding.tvWriteQuestionTextCount
        val saveButton = binding.btnWriteQuestionPetFormSave
        val maxLength = 300

        with(binding){
            editText.addTextChangedListener(object: TextWatcher {
                private var maxText = ""

                override fun afterTextChanged(s: Editable?) {
                    if (editText.length() > 0) {
                        saveButton.setEnabled(true)
                    } else {
                        saveButton.setEnabled(false)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    maxText = s.toString()
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (editText.length() > maxLength) {
                        Toast.makeText(this@QuestActivity, "최대 ${maxLength}자까지 입력 가능합니다.",
                            Toast.LENGTH_SHORT).show()

                        editText.setText(maxText)
                        editText.setSelection(editText.length())
                    }

                    countText.setText("${editText.length()}/${maxLength}")
                }

            })
        }
    }

    private fun loadQuest() {

        //val token = tokenManager.getAccessToken()
        val accessToken = tokenManager.getAccessToken()
        Log.d("token", accessToken!!)
        val questService = QuestService()
        questService.setLoadQuestView(this)
        questService.loadQuest(accessToken!!)
    }

    private fun writeQuest() {
        val dummyReq = WriteQuestRequest(
            "", "", ""
        )
        val questService = QuestService()
        questService.setWriteQuestView(this)
        questService.writeQuest(dummyReq)
    }

    override fun onSaveClickListener(id: Int) {
        binding.etWriteQuestionAnswer.setEnabled(false)
        binding.btnWriteQuestionBookmarkOff.visibility = View.VISIBLE
        binding.btnWriteQuestionPetFormSave.visibility = View.GONE

        binding.tvWriteQuestionDate.visibility = View.VISIBLE

        Toast.makeText(this, "저장 기능 구현 필요", Toast.LENGTH_SHORT).show()
    }

    override fun onLoadQuestSuccess(loadQuestData: LoadQuestData) {
        Log.d("token", "load success")
        binding.tvWriteQuestionNumber.text = "#" + loadQuestData.questId + "번째 질문"
        binding.tvWriteQuestionContent.text = loadQuestData.quest
        binding.tvWriteQuestionDate.text = loadQuestData.date
    }

    override fun onLoadQuestFailure() {
        Log.d("token", "load failure")
        //TODO("Not yet implemented")
    }

    override fun onWriteQuestSuccess() {
        //TODO("Not yet implemented")
    }

    override fun onWriteQuestFailure() {
        //TODO("Not yet implemented")
    }
}