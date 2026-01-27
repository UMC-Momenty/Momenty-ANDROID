package com.example.momenty.domain.community

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.R

class CommunityFragment : Fragment(R.layout.fragment_community) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btn_go_write).setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_communityWriteFragment)
        }

        view.findViewById<View>(R.id.btn_go_edit).setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_communityEditFragment)
        }
    }
}
