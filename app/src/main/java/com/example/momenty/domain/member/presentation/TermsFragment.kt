package com.example.momenty.domain.member.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentTermsBinding

class TermsFragment : Fragment() {

    private var _binding: FragmentTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        updateAgreeButton()
    }

    private fun setupClickListeners() {
        // 전체 동의 클릭
        binding.layoutAllAgree.setOnClickListener {
            val newState = !binding.cbAllAgree.isChecked
            setAllCheckboxes(newState)
        }

        binding.cbAllAgree.setOnClickListener {
            setAllCheckboxes(binding.cbAllAgree.isChecked)
        }

        // 개별 약관 체크박스 클릭
        binding.cbServiceTerms.setOnCheckedChangeListener { _, _ ->
            updateAllAgreeCheckbox()
            updateAgreeButton()
        }

        binding.cbPrivacyTerms.setOnCheckedChangeListener { _, _ ->
            updateAllAgreeCheckbox()
            updateAgreeButton()
        }

        binding.cbMarketingTerms.setOnCheckedChangeListener { _, _ ->
            updateAllAgreeCheckbox()
            updateAgreeButton()
        }

        // 약관 상세보기 클릭 (우측 화살표)
        binding.ivServiceArrow.setOnClickListener {
            navigateToTermsDetail("service")
        }

        binding.ivPrivacyArrow.setOnClickListener {
            navigateToTermsDetail("privacy")
        }

        binding.ivMarketingArrow.setOnClickListener {
            navigateToTermsDetail("marketing")
        }

        // 약관 텍스트 클릭 시에도 상세보기
        binding.tvServiceDetail.setOnClickListener {
            navigateToTermsDetail("service")
        }

        binding.tvPrivacyDetail.setOnClickListener {
            navigateToTermsDetail("privacy")
        }

        binding.tvMarketingDetail.setOnClickListener {
            navigateToTermsDetail("marketing")
        }

        // 동의 버튼 클릭
        binding.btnAgree.setOnClickListener {
            if (isRequiredTermsAgreed()) {
                binding.tvTermsWarning.visibility = View.GONE
                // 약관 동의 후 로그인 화면으로 이동 (저장하지 않음)
                findNavController().navigate(R.id.loginFragment)

            }else{
                binding.tvTermsWarning.visibility = View.VISIBLE
            }
        }
    }

    // 전체 동의 체크박스 상태 변경
    private fun setAllCheckboxes(isChecked: Boolean) {
        binding.cbAllAgree.isChecked = isChecked
        binding.cbServiceTerms.isChecked = isChecked
        binding.cbPrivacyTerms.isChecked = isChecked
        binding.cbMarketingTerms.isChecked = isChecked
        updateAgreeButton()
    }

    // 개별 체크박스 상태에 따라 전체 동의 체크박스 업데이트
    private fun updateAllAgreeCheckbox() {
        val allChecked = binding.cbServiceTerms.isChecked &&
                binding.cbPrivacyTerms.isChecked &&
                binding.cbMarketingTerms.isChecked

        binding.cbAllAgree.isChecked = allChecked
    }

    // 필수 약관 동의 여부 확인
    private fun isRequiredTermsAgreed(): Boolean {
        return binding.cbServiceTerms.isChecked && binding.cbPrivacyTerms.isChecked
    }

    // 동의 버튼 활성화/비활성화
    private fun updateAgreeButton() {
        val isEnabled = isRequiredTermsAgreed()
        binding.btnAgree.isEnabled = isEnabled

        // 버튼 스타일 변경
        if (isEnabled) {
            binding.btnAgree.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.caption_3)
            )
        } else {
            binding.btnAgree.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.body_2)
            )
        }
    }

    // 약관 상세 페이지로 이동
    private fun navigateToTermsDetail(termsType: String) {
        val bundle = Bundle().apply {
            putString("termsType", termsType)
        }
        findNavController().navigate(R.id.action_termsFragment_to_termsDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}