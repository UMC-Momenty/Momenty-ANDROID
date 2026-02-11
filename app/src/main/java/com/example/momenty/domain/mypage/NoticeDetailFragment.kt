package com.example.momenty.domain.mypage

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentNoticeBinding
import com.example.momenty.databinding.FragmentNoticeDetailBinding
import com.example.momenty.domain.main.presentation.MainActivity
import kotlin.getValue

class NoticeDetailFragment: Fragment() {
    lateinit var binding: FragmentNoticeDetailBinding

    private val args: NoticeDetailFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoticeDetailBinding.inflate(inflater, container, false)

        initListener()
        fillText()

        return binding.root
    }

    private fun initListener() {
        binding.btnNoticeDetailBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun fillText() {
        binding.tvNoticeDetailTitle.text = args.title
        binding.tvNoticeDetailContent.text = args.content
    }
}