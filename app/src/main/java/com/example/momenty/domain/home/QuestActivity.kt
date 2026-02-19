package com.example.momenty.domain.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.momenty.databinding.ActivityQuestBinding
import com.example.momenty.domain.mypage.MyPageRepository
import com.example.momenty.domain.mypage.MyPageRetrofitClient
import com.example.momenty.domain.mypage.MyPageViewModel
import com.example.momenty.domain.mypage.MyPageViewModelFactory
import com.example.momenty.R
import com.example.momenty.global.security.TokenManager
import kotlin.getValue

class QuestActivity/* @Inject constructor(
    private val tokenManager: TokenManager
)*/: AppCompatActivity(), ConfirmDialogInterface/*, LoadQuestView, WriteQuestView*/ {

    lateinit var binding: ActivityQuestBinding

    private var loadQuestByApi: LoadQuestData ?= null

    private var bSuccessApi = false
    private val TAG = "QuestActivity"

    /*
    private val questViewModel: QuestViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val service: QuestService = KhgApiClient.questService
                val repository = QuestRepository(service)
                return QuestViewModel(repository) as T
            }
        }
    }*/

    private val questViewModel: QuestViewModel by viewModels {
        val repo = QuestRepository(
            service = QuestRetrofitClient.questService
        )
        QuestViewModelFactory(repo)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuestBinding.inflate(layoutInflater)

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

        val btnGood = binding.rbWriteGood
        val btnSoso= binding.rbWriteSoso
        val btnBad = binding.rbWriteBad

        when(binding.rgWriteQuestion.checkedRadioButtonId) {
            R.id.rb_write_good -> {
                btnBad.visibility = View.GONE
                btnSoso.visibility = View.GONE
            }
            R.id.rb_write_soso -> {
                btnBad.visibility = View.GONE
                btnGood.visibility = View.GONE
            }
            R.id.rb_write_bad -> {
                btnGood.visibility = View.GONE
                btnSoso.visibility = View.GONE
            }
            else -> {
                binding.rgWriteQuestion.visibility = View.GONE
            }
        }

        performWriteQuest()
    }



    private fun performLoadQuest() {
        questViewModel.loadQuest()
    }

    private fun performWriteQuest() {

        var questId = 0L
        var petId = 0L
        val answer = binding.etWriteQuestionAnswer.toString()

        if (loadQuestByApi != null) {
            questId = loadQuestByApi!!.questId

        }

        questViewModel.writeQuest(questId, petId, answer)
    }

    private fun observeQuest() {
        questViewModel.writeQuestResult.observe(this) {result ->
            result.onSuccess { data ->
                bSuccessApi = true
                Toast.makeText(this, "질문 작성 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
                bSuccessApi = false
            }.onFailure { error ->
                bSuccessApi = false
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(this, "질문 작성 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "질문 작성 실패: $message")
            }
        }

        questViewModel.loadQuestResult.observe(this) {result ->
            result.onSuccess { data ->
                bSuccessApi = true
                Toast.makeText(this, "질문 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "로드 데이터: $data")
                loadQuestByApi = data
                binding.tvWriteQuestionNumber.text = "#${data.questId.toString()}번째 질문"
                binding.tvWriteQuestionContent.text = data.quest
                binding.tvWriteQuestionDate.text = data.date
                bSuccessApi = false
            }.onFailure { error ->
                bSuccessApi = false
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(this, "질문 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "질문 로드 실패: $message")
            }
        }
    }
}