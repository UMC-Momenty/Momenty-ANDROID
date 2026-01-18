package com.example.momenty.domain.main.presentation

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "momenty_prefs"
        private const val KEY_TERMS_AGREED = "terms_agreed"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"

        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: PreferenceManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // 약관 동의 관련
    var isTermsAgreed: Boolean
        get() = prefs.getBoolean(KEY_TERMS_AGREED, false)
        set(value) = prefs.edit().putBoolean(KEY_TERMS_AGREED, value).apply()

    // 로그인 관련
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    // 사용자 ID
    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    // 사용자 이름
    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    // 로그아웃
    fun logout() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putBoolean(KEY_TERMS_AGREED, false)  // 약관 동의도 초기화
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            apply()
        }
    }

    // 모든 데이터 초기화 (앱 재설치 시뮬레이션)
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}