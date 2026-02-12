package com.example.momenty.domain.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.databinding.FragmentTermsDetailBinding

class TermsDetailFragment : Fragment() {

    private var _binding: FragmentTermsDetailBinding? = null
    private val binding get() = _binding!!

    private var termsType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        termsType = arguments?.getString("termsType")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupListeners()
    }

    private fun setupView() {
        when (termsType) {
            "service" -> {
                binding.tvTermsTitle.text = "서비스 이용약관"
                binding.tvTermsContent.text = getServiceTermsContent()
            }
            "privacy" -> {
                binding.tvTermsTitle.text = "개인정보 수집/이용 동의"
                binding.tvTermsContent.text = getPrivacyTermsContent()
            }
            "marketing" -> {
                binding.tvTermsTitle.text = "마케팅 수신 동의"
                binding.tvTermsContent.text = getMarketingTermsContent()
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun getServiceTermsContent(): String {
        return """
제1조 (목적)
이 약관은 Momenty(이하 "회사")가 제공하는 서비스의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.

제2조 (정의)
1. "서비스"란 회사가 제공하는 반려동물 기록 및 관리 서비스를 의미합니다.
2. "이용자"란 본 약관에 따라 회사가 제공하는 서비스를 이용하는 자를 말합니다.
3. "계정"이란 서비스 이용을 위해 이용자가 설정한 고유 정보를 말합니다.

제3조 (약관의 효력 및 변경)
1. 본 약관은 서비스를 이용하고자 하는 모든 이용자에 대하여 그 효력을 발생합니다.
2. 회사는 필요한 경우 관련 법령을 위배하지 않는 범위에서 본 약관을 변경할 수 있습니다.

제4조 (서비스의 제공)
1. 회사는 다음과 같은 서비스를 제공합니다:
   - 반려동물 일상 기록
   - 사진 및 동영상 저장
   - 건강 정보 관리
   - 추억 공유 기능

제5조 (이용자의 의무)
1. 이용자는 본 약관 및 관련 법령을 준수해야 합니다.
2. 이용자는 타인의 권리를 침해하는 행위를 해서는 안 됩니다.
        """.trimIndent()
    }

    private fun getPrivacyTermsContent(): String {
        return """
개인정보 처리방침

Momenty(이하 "회사")는 이용자의 개인정보를 중요시하며, 개인정보보호법 등 관련 법령을 준수합니다.

1. 수집하는 개인정보 항목
가. 필수항목
   - 소셜 로그인 정보 (이메일, 프로필 정보)
   - 서비스 이용 기록

나. 선택항목
   - 반려동물 정보 (이름, 종류, 생년월일)
   - 프로필 사진

2. 개인정보의 수집 및 이용목적
가. 서비스 제공 및 개선
나. 회원 관리 및 본인 확인
다. 서비스 관련 공지사항 전달
라. 통계 분석 및 서비스 품질 향상

3. 개인정보의 보유 및 이용기간
가. 회원 탈퇴 시까지 보유
나. 관련 법령에 따라 일정 기간 보관이 필요한 경우 해당 기간 동안 보관

4. 개인정보의 제3자 제공
회사는 원칙적으로 이용자의 개인정보를 외부에 제공하지 않습니다.

5. 이용자의 권리
이용자는 언제든지 자신의 개인정보를 조회하거나 수정할 수 있으며, 회원 탈퇴를 요청할 수 있습니다.
        """.trimIndent()
    }

    private fun getMarketingTermsContent(): String {
        return """
마케팅 정보 수신 동의 (선택)

Momenty는 아래와 같은 마케팅 정보를 발송할 수 있습니다.

1. 발송 내용
가. 이벤트 및 프로모션 안내
나. 신규 서비스 및 기능 소개
다. 맞춤형 콘텐츠 추천
라. 서비스 관련 소식 및 혜택 정보

2. 발송 방법
가. 이메일
나. 푸시 알림 (앱 알림)
다. 앱 내 메시지

3. 유의사항
가. 본 동의는 선택사항이며, 동의하지 않으셔도 서비스 이용에 제한이 없습니다.
나. 마케팅 정보 수신을 원하지 않으실 경우, 언제든지 앱 설정에서 수신 거부를 하실 수 있습니다.
다. 수신 동의 철회 후에도 서비스 이용에 필요한 필수 공지사항은 발송될 수 있습니다.

4. 동의 철회
설정 > 알림 설정에서 언제든지 마케팅 정보 수신을 철회할 수 있습니다.
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}