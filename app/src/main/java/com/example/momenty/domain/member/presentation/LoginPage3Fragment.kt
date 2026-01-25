package com.example.momenty.domain.member.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentLoginPage3Binding
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavOptions


@AndroidEntryPoint
class LoginPage3Fragment : Fragment() {

    private var _binding: FragmentLoginPage3Binding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginPage3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeLoginState()
    }

    private fun setupClickListeners() {
        binding.btnKakaoLogin.setOnClickListener {
            handleKakaoLogin()
        }

        binding.btnGoogleLogin.setOnClickListener {
            handleGoogleLogin()
        }

        binding.btnNaverLogin.setOnClickListener {
            handleNaverLogin()
        }
    }

    private fun observeLoginState() {
        authViewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Idle -> {
                    hideLoading()
                }
                is LoginState.Loading -> {
                    showLoading()
                }
                is LoginState.Success -> {
                    hideLoading()
                    handleLoginSuccess(state.user.uid, state.user.displayName)
                }
                is LoginState.Error -> {
                    hideLoading()
                    handleLoginError(state.message, state.code)
                }
            }
        }
    }

    private fun handleKakaoLogin() {
        authViewModel.loginWithKakao()
    }

    private fun handleGoogleLogin() {
        // TODO: 구글 로그인 구현
        Toast.makeText(requireContext(), "구글 로그인 준비 중입니다", Toast.LENGTH_SHORT).show()
    }

    private fun handleNaverLogin() {
        // TODO: 네이버 로그인 구현
        Toast.makeText(requireContext(), "네이버 로그인 준비 중입니다", Toast.LENGTH_SHORT).show()
    }

    private fun handleLoginSuccess(userId: String, userName: String?) {
        Log.d(TAG, "로그인 성공: userId=$userId, userName=$userName")

        Toast.makeText(
            requireContext(),
            "${userName ?: "사용자"}님, 환영합니다!",
            Toast.LENGTH_SHORT
        ).show()

        // MainActivity에 사용자 정보 저장 (기존 로직 유지)
        (activity as? com.example.momenty.domain.main.presentation.MainActivity)?.apply {
            saveLoggedIn(userId, userName ?: "사용자")
        }

        // 메인 화면으로 이동
        navigateToRecord()
    }

    private fun handleLoginError(message: String, code: String?) {
        Log.e(TAG, "로그인 실패: code=$code, message=$message")

        val errorMessage = when (code) {
            "MEMBER4001" -> "이미 가입된 사용자입니다"
            "MEMBER4002" -> "사용자를 찾을 수 없습니다"
            "MEMBER5001" -> "서버 오류가 발생했습니다"
            else -> message
        }

        Toast.makeText(
            requireContext(),
            errorMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading() {
        binding.btnKakaoLogin.isEnabled = false
        binding.btnGoogleLogin.isEnabled = false
        binding.btnNaverLogin.isEnabled = false

        // TODO: 프로그레스바 표시
        // binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.btnKakaoLogin.isEnabled = true
        binding.btnGoogleLogin.isEnabled = true
        binding.btnNaverLogin.isEnabled = true

        // TODO: 프로그레스바 숨김
        // binding.progressBar.visibility = View.GONE
    }

    private fun navigateToRecord() {
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.auth_graph, true) // inclusive = true와 동일
            .setLaunchSingleTop(true)
            .build()

        findNavController().navigate(R.id.home_graph, null, options)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.resetState()
        _binding = null
    }

    companion object {
        private const val TAG = "LoginPage3Fragment"
    }
}