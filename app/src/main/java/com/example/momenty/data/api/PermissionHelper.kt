package com.example.momenty.data.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * 권한 요청 헬퍼
 */
class PermissionHelper(
    private val fragment: Fragment,
    private val permissionLauncher: ActivityResultLauncher<Array<String>>
) {

    /**
     * 이미지 선택에 필요한 권한 확인 및 요청
     */
    fun checkAndRequestImagePermissions(onGranted: () -> Unit) {
        val permissions = getRequiredPermissions()

        if (hasPermissions(fragment.requireContext(), permissions)) {
            onGranted()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    /**
     * 필요한 권한 목록 반환
     */
    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * 모든 권한이 허용되었는지 확인
     */
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        /**
         * 권한 결과 확인
         */
        fun areAllPermissionsGranted(permissions: Map<String, Boolean>): Boolean {
            return permissions.values.all { it }
        }
    }
}