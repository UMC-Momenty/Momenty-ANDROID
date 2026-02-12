package com.example.momenty.domain.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.momenty.databinding.FragmentAppInfoTermsBinding

class AppInfoTermsFragment: Fragment() {
    lateinit var binding: FragmentAppInfoTermsBinding

    private val args: AppInfoTermsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppInfoTermsBinding.inflate(inflater, container, false)

        initListener()
        fillText()

        return binding.root
    }

    private fun initListener() {
        binding.btnAppInfoTermsClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun fillText() {
        binding.tvAppInfoTermsContentTitle.text = args.title
        binding.tvAppInfoTermsContentBody.text = args.content
    }
}