package com.example.momenty.domain.member.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentLoginPage3Binding
import com.example.momenty.domain.main.presentation.MainActivity

class LoginPage3Fragment : Fragment() {

    private var _binding: FragmentLoginPage3Binding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupClickListeners() {
        binding.btnKakaoLogin.setOnClickListener {
            handleKakaoLogin()
        }

        binding.btnGoogleLogin.setOnClickListener {
            handleGoogleLogin()
        }

        binding.btnNaverLogin.setOnClickListener{
            handleNaverLogin()
        }
    }

    private fun handleKakaoLogin() {
        // TODO: 카카오 로그인 구현
        // 로그인 성공 후
        val userId = "kakao_user_id"  // 실제 카카오에서 받은 ID
        val userName = "사용자 이름"   // 실제 카카오에서 받은 이름

        (activity as? MainActivity)?.saveLoggedIn(userId, userName)
        navigateToRecord()
    }

    private fun handleGoogleLogin() {
        // TODO: 구글 로그인 구현
        // 로그인 성공 후
        val userId = "google_user_id"  // 실제 구글에서 받은 ID
        val userName = "사용자 이름"    // 실제 구글에서 받은 이름

        (activity as? MainActivity)?.saveLoggedIn(userId, userName)
        navigateToRecord()
    }

    private fun handleNaverLogin(){
        // TODO: 구글 로그인 구현
        // 로그인 성공 후
        val userId = "naver_user_id"  // 실제 구글에서 받은 ID
        val userName = "사용자 이름"    // 실제 구글에서 받은 이름

        (activity as? MainActivity)?.saveLoggedIn(userId, userName)
        navigateToRecord()
    }

    private fun navigateToRecord() {
        // LoginFragment의 부모에서 navController를 찾아서 이동
        parentFragment?.parentFragment?.let { loginFragment ->
            if (loginFragment is LoginFragment) {
                loginFragment.findNavController()
                    .navigate(R.id.action_loginFragment_to_recordFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}