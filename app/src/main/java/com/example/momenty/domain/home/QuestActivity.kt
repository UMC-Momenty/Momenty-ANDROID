package com.example.momenty.domain.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.momenty.databinding.ActivityQuestBinding
import com.example.momenty.domain.calendar.RetrofitClient
import com.example.momenty.global.security.TokenManager

class QuestActivity/* @Inject constructor(
    private val tokenManager: TokenManager
)*/: AppCompatActivity(), ConfirmDialogInterface/*, LoadQuestView, WriteQuestView*/ {

    lateinit var binding: ActivityQuestBinding
    lateinit var tokenManager: TokenManager

    lateinit var myLoadQuestResp: LoadQuestData
    private val TAG = "QuestActivity"

    private val questViewModel: QuestViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val service: QuestService = QuestApiClient.questService
                val repository = QuestRepository(service)
                return QuestViewModel(repository) as T
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuestBinding.inflate(layoutInflater)
        tokenManager = TokenManager(this)

        setContentView(binding.root)

        observeQuest()
        textCount()
        initListener()
        performLoadQuest()

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
            binding.btnWriteQuestionBookmarkOn.visibility = View.GONE
            binding.btnWriteQuestionBookmarkOff.visibility = View.VISIBLE
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

                    countText.text = "${editText.length()}/${maxLength}"
                }

            })
        }
    }

    /*
    private fun loadQuest() {

        //val token = tokenManager.getAccessToken()
        val accessToken = tokenManager.getAccessToken()
        Log.d("token", accessToken!!)
        val questService1 = QuestService1()
        questService1.setLoadQuestView(this)
        questService1.loadQuestAPI(accessToken!!)
    }

    private fun writeQuest() {
        val answer = binding.etWriteQuestionAnswer.text.toString()
        val dummyReq = WriteQuestRequest(
            myLoadQuestResp.questId, "1", answer
        )
        val questService1 = QuestService1()
        questService1.setWriteQuestView(this)
        questService1.writeQuestAPI(dummyReq)
    }
    */

    override fun onSaveClickListener(id: Int) {
        binding.etWriteQuestionAnswer.setEnabled(false)
        binding.btnWriteQuestionBookmarkOff.visibility = View.VISIBLE
        binding.btnWriteQuestionPetFormSave.visibility = View.GONE

        binding.tvWriteQuestionDate.visibility = View.VISIBLE

        Toast.makeText(this, "저장 기능 구현 필요", Toast.LENGTH_SHORT).show()
        performWriteQuest()
    }

    /*
    override fun onLoadQuestSuccess(loadQuestData: LoadQuestData) {
        Log.d("LoadQuest/QuestAct", "load success")
        myLoadQuestResp = loadQuestData
        binding.tvWriteQuestionNumber.text = "#" + loadQuestData.questId + "번째 질문"
        binding.tvWriteQuestionContent.text = loadQuestData.quest
        binding.tvWriteQuestionDate.text = loadQuestData.date
    }

    override fun onLoadQuestFailure() {
        Log.d("LoadQuest/QuestAct", "load failure")
        //TODO("Not yet implemented")
    }

    override fun onWriteQuestSuccess() {
        Log.d("WriteQuest/QuestAct", "write success")
        //TODO("Not yet implemented")
    }

    override fun onWriteQuestFailure() {
        Log.d("WriteQuest/QuestAct", "write failure")
        //TODO("Not yet implemented")
    }
    */


    private fun performLoadQuest() {
        val accessToken = tokenManager.getAccessToken()
        questViewModel.loadQuest(accessToken!!)
    }

    private fun performWriteQuest() {
        val questId = binding.tvWriteQuestionNumber.toString()
        val petId = ""
        val answer = binding.etWriteQuestionAnswer.toString()

        val accessToken = tokenManager.getAccessToken()

        questViewModel.writeQuest(accessToken!!, questId, petId, answer)
    }

    private fun observeQuest() {
        questViewModel.writeQuestResult.observe(this) {result ->
            result.onSuccess { data ->
                Toast.makeText(this, "질문 작성 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(this, "질문 작성 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "질문 작성 실패: $message")
            }
        }

        questViewModel.loadQuestResult.observe(this) {result ->
            result.onSuccess { data ->
                Toast.makeText(this, "질문 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "로드 데이터: $data")
                binding.tvWriteQuestionNumber.text = data.questId
                binding.tvWriteQuestionContent.text = data.quest
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(this, "질문 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "질문 로드 실패: $message")
            }
        }
    }
}