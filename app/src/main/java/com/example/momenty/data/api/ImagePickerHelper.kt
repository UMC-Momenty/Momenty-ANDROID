package com.example.momenty.data.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import java.io.File

/**
 * 이미지 선택 헬퍼 클래스
 */
class ImagePickerHelper(
    private val context: Context,
    private val galleryLauncher: ActivityResultLauncher<Intent>,
    private val cameraLauncher: ActivityResultLauncher<Uri>
) {

    private var tempImageUri: Uri? = null

    /**
     * 이미지 선택 다이얼로그 표시
     */
    fun showImagePickerDialog() {
        val options = arrayOf("갤러리에서 선택", "카메라로 촬영")

        AlertDialog.Builder(context)
            .setTitle("프로필 이미지 선택")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 갤러리 열기
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        galleryLauncher.launch(intent)
    }

    /**
     * 카메라 열기
     */
    private fun openCamera() {
        // 임시 파일 생성
        val photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        tempImageUri = photoUri
        cameraLauncher.launch(photoUri)
    }

    /**
     * 임시 이미지 파일 생성
     */
    private fun createImageFile(): File {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.cacheDir
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    /**
     * 카메라 촬영 후 임시 URI 가져오기
     */
    fun getTempImageUri(): Uri? = tempImageUri

    companion object {
        const val REQUEST_CODE_GALLERY = 1001
        const val REQUEST_CODE_CAMERA = 1002
    }
}