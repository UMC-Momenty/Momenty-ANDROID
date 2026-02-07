package com.example.momenty.domain.member.presentation

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentLoginPage3Binding
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavOptions
import com.example.momenty.ui.auth.AuthUiState
import com.example.momenty.ui.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginPage3Fragment : Fragment() {

    private var _binding: FragmentLoginPage3Binding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    Log.d(TAG, "Google account received: ${it.email}")
                    // ViewModel에 위임
                    authViewModel.loginWithGoogle(it)
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed: statusCode=${e.statusCode}", e)
                showToast("구글 로그인 실패: ${e.message}")
            }
        } else {
            Log.d(TAG, "Google sign in cancelled")
        }
    }

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
        // 카카오 로그인 : ViewModel에 위임
        binding.btnKakaoLogin.setOnClickListener {
            authViewModel.loginWithKakao()
        }

        // 구글 로그인 : Intent만 실행, 결과는 ViewModel로
        binding.btnGoogleLogin.setOnClickListener {
            authViewModel.startGoogleSignIn(googleSignInLauncher)
        }

        // 네이버 로그인
        binding.btnNaverLogin.setOnClickListener {
            authViewModel.loginWithNaver(requireContext())
        }
    }

    private fun observeLoginState() {
        viewLifecycleOwner.lifecycleScope.launch{
            authViewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Idle -> {
                        hideLoading()
                    }
                    is AuthUiState.Loading -> {
                        showLoading()
                    }
                    is AuthUiState.Success -> {
                        hideLoading()
                        handleLoginSuccess(state.userName)
                    }
                    is AuthUiState.Error -> {
                        hideLoading()
                        handleLoginError(state.message)
                    }
                }
            }
        }
    }

    private fun handleLoginSuccess(userName: String?) {
        Log.d(TAG, "로그인 성공: userName=$userName")

        showToast("${userName ?: "사용자"}님, 환영합니다!")

        // MainActivity에 사용자 정보 저장 (기존 로직 유지)
        (activity as? com.example.momenty.domain.main.presentation.MainActivity)?.apply {
            saveLoggedIn("temp_user_id", userName ?: "사용자")
        }

        // 메인 화면으로 이동
        navigateToRecord()
    }

    private fun handleLoginError(message: String) {
        Log.e(TAG, "로그인 실패: $message")
        showToast("로그인 실패: $message")
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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