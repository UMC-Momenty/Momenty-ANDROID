package com.example.momenty.domain.mypage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.momenty.R
import com.example.momenty.data.api.ImagePickerHelper
import com.example.momenty.data.api.PermissionHelper
import com.example.momenty.databinding.FragmentCustomerCenterInquiryWriteBinding
import com.example.momenty.domain.home.KhgApiClient
import com.example.momenty.global.security.TokenManager
import java.io.File
import java.io.FileOutputStream
import kotlin.getValue

class CustomerCenterInquiryWriteFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterInquiryWriteBinding
    lateinit var tokenManager: TokenManager

    private val TAG = "InqWriteFrag"
    var imageFiles = mutableListOf<File>()
    var bContent = false

    var imgUri1: Uri ?= null
    var imgUri2: Uri ?= null

    var imgKey1: String ?= null
    var imgKey2: String ?= null
    val imgKey = ArrayList<String>()


    private val myPageViewModel: MyPageViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val service: MyPageService = KhgApiClient.myPageService
                val repository = MyPageRepository(service)
                return MyPageViewModel(repository) as T
            }
        }
    }

    private val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(2)) { uris ->
        if (uris.isNotEmpty()) {
            Log.d(TAG, "선택된 URI 개수: ${uris.size}")

            imageFiles.clear()
            binding.ivInquiryWriteAddedPhoto1.setImageResource(0)
            binding.ivInquiryWriteAddedPhoto2.setImageResource(0)

            // 선택된 이미지들을 순회하며 처리
            for ((index, uri) in uris.withIndex()) {
                processImage(uri, index)
            }
        } else {
            Log.d(TAG, "사진 선택 취소됨")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerCenterInquiryWriteBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())

        observePerformAddInquiry()
        observePerformGetImageUrl()

        setDropdown()
        initListener()

        return binding.root
    }

    private fun initListener() {
        binding.spInquiryWriteType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // TODO("드롭다운 내용 전달")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        textCountListener()

        binding.layoutInquiryWriteAddPhotoOff.setOnClickListener {


            //selectGallery()
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            performGetImageUrl()
        }

        binding.btnInquiryWriteSubmit.setOnClickListener {
            performAddInquiry()
            findNavController().navigateUp()
        }
    }

    private fun setDropdown() {
        val inquiryType = resources.getStringArray(R.array.inquiry_type)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            inquiryType
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spInquiryWriteType.adapter = adapter
    }

    private fun textCountListener() {
        val editText = binding.etInquiryWriteContent
        val countText = binding.tvInquiryWriteTextCount
        val submitButton = binding.btnInquiryWriteSubmit
        val maxLength = 500

        with(binding) {
            editText.addTextChangedListener(object : TextWatcher {
                private var maxText = ""
                override fun afterTextChanged(s: Editable?) {
                    if (editText.length() > 0) {
                        submitButton.setEnabled(true)
                    } else {
                        submitButton.setEnabled(false)
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
                        Toast.makeText(
                            activity,
                            "최대 ${maxLength}자까지 입력 가능합니다.",
                            Toast.LENGTH_SHORT).show()

                        editText.setText(maxText)
                        editText.setSelection(editText.length())
                    }

                    countText.text = "${editText.length()}/${maxLength}"
                }
            })
        }
    }

    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data

            imageFiles.clear()
            binding.ivInquiryWriteAddedPhoto1.setImageResource(0)
            binding.ivInquiryWriteAddedPhoto2.setImageResource(0)

            if (data?.clipData != null) {
                val clipData = data.clipData!!
                val count = clipData.itemCount

                if (count > 2) {
                    Toast.makeText(context, "사진은 최대 2장까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                }

                val limit = if (count > 2) 2 else count

                for (i in 0 until limit) {
                    val imageUri = clipData.getItemAt(i).uri
                    processImage(imageUri, i)
                }
            }

            else if (data?.data != null) {
                val imageUri = data.data!!
                processImage(imageUri, 0)
            }
        }
    }

    private fun processImage(uri: Uri, index: Int) {
        val tempFile = createTempFileFromUri(uri)
        if (tempFile != null) {
            imageFiles.add(tempFile)
        } else {
            Toast.makeText(requireContext(), "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        /*
        try {
            val path = getRealPathFromURI(uri)
            imageFiles.add(File(path))
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

        lateinit var targetImageView: ImageView

        if (index == 0) {
            targetImageView = binding.ivInquiryWriteAddedPhoto1
            binding.ivInquiryWriteAddedPhoto1.visibility = View.VISIBLE
            binding.layoutInquiryWriteAddPhotoOff.visibility = View.GONE
            binding.layoutInquiryWriteAddPhotoOn.visibility = View.VISIBLE
            imgUri1 = uri
        } else {
            targetImageView = binding.ivInquiryWriteAddedPhoto2
            binding.ivInquiryWriteAddedPhoto2.visibility = View.VISIBLE
            imgUri2 = uri
        }

        Glide.with(this)
            .load(uri)
            .fitCenter()
            .apply(RequestOptions().override(500,500))
            .into(targetImageView)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var columnIndex = 0
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(
            uri, proj, null, null, null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val result = cursor.getString(columnIndex)
            cursor.close()
            result
        } else {
            ""
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            // 캐시 디렉토리에 임시 파일 생성
            val tempFile = File(requireContext().cacheDir, "temp_img_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)

            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun selectGallery() {
        // Fragment가 Activity에 attach되어 있는지 확인
        if (!isAdded || context == null) {
            Log.e("CustomerCenter", "Fragment not attached to activity")
            return
        }

        val writePermission = ContextCompat.checkSelfPermission(
            requireContext(),  // ← context를 requireContext()로 변경
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            requireContext(),  // ← context를 requireContext()로 변경
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED ||
            readPermission == PackageManager.PERMISSION_DENIED) {
            Log.d("myTag", "need permission")
            requestPermissions(  // ← ActivityCompat.requestPermissions 대신 Fragment의 requestPermissions 사용
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQ_GALLERY
            )
        } else {
            Log.d("myTag", "running")
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            imageResult.launch(Intent.createChooser(intent, "사진을 선택하세요"))
        }
    }

    private fun performAddInquiry() {
        val type = binding.spInquiryWriteType.selectedItem as? String
        val content = binding.etInquiryWriteContent.text.toString()
        val images = ArrayList<AddInquiryRequestImg>().apply {
            for (iter in imgKey) {
                add(AddInquiryRequestImg(iter))
            }
            /*
            if (imgUri1 != null) {
                add(AddInquiryRequestImg(imgUri1.toString()))
            }
            if (imgUri2 != null) {
                add(AddInquiryRequestImg(imgUri2.toString()))
            }*/
        }
        val req = AddInquiryRequest(
            type!!, content, images
        )

        val accessToken = tokenManager.getAccessToken()
        val userId = tokenManager.getUserId()
        myPageViewModel.addInquiry(accessToken!!, userId, req)
    }

    private fun performGetImageUrl() {
        val imagesType = ArrayList<String>().apply {
            if (imgUri1 != null) {
                add(imgUri1.toString())
            }
            if (imgUri2 != null) {
                add(imgUri2.toString())
            }
        }
        val req = GetImageUrlRequest(imagesType)

        val accessToken = tokenManager.getAccessToken()
        myPageViewModel.getImageUrl(accessToken!!, req)
    }

    private fun observePerformAddInquiry() {
        myPageViewModel.addInquiryResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "문의하기 성공!", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireContext(), "문의하기 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "문의하기 실패: $message")
            }
        }
    }

    private fun observePerformGetImageUrl() {
        myPageViewModel.getImageUrlResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "이미지 키 변환 성공!", Toast.LENGTH_SHORT).show()
                for (iter in data) {
                    if (iter != null) {
                        imgKey.add(iter.key)
                    }
                }
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireContext(), "이미지 키 변환 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "이미지 키 변환 실패: $message")
            }
        }
    }


    companion object{
        const val REVIEW_MIN_LENGTH = 10
        const val REQ_GALLERY = 1

        const val PARAM_KEY_IMAGE = "image"
        const val PARAM_KEY_PRODUCT_ID = "product_id"
        const val PARAM_KEY_REVIEW = "review_content"
        const val PARAM_KEY_RATING = "rating"
    }
}