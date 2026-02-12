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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCustomerCenterInquiryWriteBinding
import java.io.File

class CustomerCenterInquiryWriteFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterInquiryWriteBinding

    var imageFiles = mutableListOf<File>()
    var bContent = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerCenterInquiryWriteBinding.inflate(inflater, container, false)

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
            selectGallery()
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
        try {
            val path = getRealPathFromURI(uri)
            imageFiles.add(File(path))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        lateinit var targetImageView: ImageView

        if (index == 0) {
            targetImageView = binding.ivInquiryWriteAddedPhoto1
            binding.ivInquiryWriteAddedPhoto1.visibility = View.VISIBLE
            binding.layoutInquiryWriteAddPhotoOff.visibility = View.GONE
            binding.layoutInquiryWriteAddPhotoOn.visibility = View.VISIBLE
        } else {
            targetImageView = binding.ivInquiryWriteAddedPhoto2
            binding.ivInquiryWriteAddedPhoto2.visibility = View.VISIBLE
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

    companion object{
        const val REVIEW_MIN_LENGTH = 10
        const val REQ_GALLERY = 1

        const val PARAM_KEY_IMAGE = "image"
        const val PARAM_KEY_PRODUCT_ID = "product_id"
        const val PARAM_KEY_REVIEW = "review_content"
        const val PARAM_KEY_RATING = "rating"
    }
}