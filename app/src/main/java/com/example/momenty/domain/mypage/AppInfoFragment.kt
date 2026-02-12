package com.example.momenty.domain.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.databinding.FragmentAppInfoBinding

class AppInfoFragment: Fragment() {
    lateinit var binding: FragmentAppInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppInfoBinding.inflate(inflater, container, false)

        initListener()

        return binding.root
    }

    private fun initListener() {
        binding.btnAppInfoBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 더미 데이터 입력
        binding.layoutAppInfoTerms.setOnClickListener {
            val title = "제 1장 총칙"
            val content = "제1조 (목적) 이 약관은 모먼티(이하 “회사”라 합니다)가 모바일 기기를 통해 제공하는 게임 서비스 및 이에 부수하는 네트워크, 웹사이트, 기타 서비스(이하 “서비스”라 합니다)의 이용에 대한 회사와 서비스 이용자의 권리ㆍ의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.\n" +
                    "\n" +
                    "제2조 (용어의 정의) ① 이 약관에서 사용하는 용어의 정의는 다음과 같습니다.\n" +
                    " 1. “회사”라 함은 모바일 기기를 통하여 서비스를 제공하는 사업자를 의미합니다.\n" +
                    " 2. “회원”이란 이 약관에 따라 이용계약을 체결하고, 회사가 제공하는 서비스를 이용하는 자를 의미합니다.\n" +
                    " 3. “모바일 기기”란 콘텐츠를 다운로드 받거나 설치하여 사용할 수 있는 기기로서, 휴대폰, 스마트폰, 태블릿 등을 의미합니다.\n" +
                    " 4. “계정정보”란 회원의 회원번호와 외부계정정보, 기기정보, 닉네임, 프로필 사진, 친구목록 등 회원이 회사에 제공한 정보와 이용정보 (캐릭터 정보, 아이템 등), 이용요금 결제 정보 등을 통칭합니다.\n" +
                    " 5. “콘텐츠”란 모바일 기기로 이용할 수 있도록 회사가 서비스 제공과 관련하여 디지털 방식으로 제작한 유료 또는 무료의 내용물 일체(이미지 및 네트워크 서비스, 애플리케이션, 아이템 등)를 의미합니다.\n" +
                    " 6. “오픈마켓”이란 모바일 기기에서 콘텐츠를 설치하고 결제할 수 있도록 구축된 전자상거래 환경을 의미합니다.\n" +
                    " 7. “애플리케이션”이란 회사가 제공하는 서비스를 이용하기 위하여 모바일 기기를 통해 다운로드 받거나 설치하여 사용하는 프로그램 일체를 의미합니다.\n" +
                    " 8. “서비스”라 함은 회사가 제공하는 서비스의 하나로서 회원이 모바일 기기에서 실행하는 일기장 작성 및 공유와 이에 부수하는 서비스를 의미합니다.\n" +
                    "② 이 약관에서 사용하는 용어의 정의는 본 조 제1항에서 정하는 것을 제외하고는 관계법령 및 서비스별 정책에서 정하는 바에 의하며, 이에 정하지 아니한 것은 일반적인 상 관례에 따릅니다."

            this@AppInfoFragment.findNavController().navigate(
                AppInfoFragmentDirections.actionAppInfoFragmentToAppInfoTermsFragment(
                    title, content
                )
            )
        }

        binding.layoutAppInfoPrivacy.setOnClickListener {
            val title = "개인정보 수집 및 이용에 대한 안내(필수)"
            val content = "<모먼티>는 정보주체의 자유와 권리 보호를 위해 「개인정보 보호법」 및 관계 법령이 정한 바를 준수하여, 적법하게 개인정보를 처리하고 안전하게 관리하고 있습니다. 이에 「개인정보 보호법」 제30조에 따라 정보주체에게 개인정보 처리에 관한 절차 및 기준을 안내하고, 이와 관련한 고충을 신속하고 원활하게 처리할 수 있도록 하기 위하여 다음과 같이 개인정보 처리방침을 수립・공개합니다.\n" +
                    "\n" +
                    "□ 개인정보의 처리 목적\n" +
                    "① <모먼티>는 다음의 목적을 위하여 개인정보를 처리합니다. 처리하고 있는 개인정보는 다음의 목적 이외의 용도로는 이용되지 않으며, 이용 목적이 변경되는 경우에는 「개인정보 보호법」 제18조에 따라 별도의 동의를 받는 등 필요한 조치를 이행할 예정입니다.\n" +
                    " 1. 회원 가입 및 관리\n" +
                    " 회원 가입의사 확인, 본인 식별・인증, 만14세 이상 여부 확인, 회원자격 유지・관리, 서비스 부정이용 방지, 각종 고지・통지 등을 목적으로 개인정보를 처리합니다.\n" +
                    " 2. 민원사무 처리\n" +
                    " 민원인의 신원 확인, 민원사항 확인, 사실조사를 위한 연락・통지, 처리결과 통보 등의 목적으로 개인정보를 처리합니다.\n" +
                    " 3. 마케팅 및 광고에의 활용\n" +
                    "　맞춤형 광고 제공 등\n" +
                    " 4. 서비스 제공\n" +
                    " 요금 결제・정산, 본인인증, 연령인증, 기본/유료 서비스 제공\n" +
                    " 00. <기타 공공기관의 개인정보 처리업무>\n" +
                    " <개인정보 처리업무에 따른 처리목적>으로 개인정보를 처리합니다.\n" +
                    "\n" +
                    "□ 개인정보 처리 항목\n" +
                    "① <모먼티>는 서비스 이용자에 대해 다음의 개인정보항목을 수집하여 처리하고 있습니다.\n" +
                    "스 이용 시\n" +
                    "  구입 내역"

            this@AppInfoFragment.findNavController().navigate(
                AppInfoFragmentDirections.actionAppInfoFragmentToAppInfoTermsFragment(
                    title, content
                )
            )
        }
    }
}