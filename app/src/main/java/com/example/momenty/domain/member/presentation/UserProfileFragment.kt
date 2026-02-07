package com.example.momenty.domain.member.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.momenty.databinding.FragmentSignupUserProfileBinding

class UserProfileFragment : Fragment() {
    private var _binding: FragmentSignupUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
}