package com.example.momenty.domain.record

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentRecordBinding

class RecordFragment : Fragment(R.layout.fragment_record) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRecordBinding.bind(view)


        val dummyList = listOf(
            RecordItem(
                "2025.11.21",
                "즐거운 날",
                "즐거움",
                "오늘은 특별한 날이었어요!",
                R.drawable.dummy1
            ),
            RecordItem(
                "2025.10.31",
                "슬픔",
                "우울한 하루였어요",
                "내용 작성~~~",
                R.drawable.dummy2
            ),
            RecordItem(
                "2025.09.15",
                "보통",
                "그냥 그런 하루",
                "오늘은 평범했어요",
                R.drawable.dummy3
            )
        )


        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.topBar.updatePadding(top = top)


            insets
        }

        ViewCompat.requestApplyInsets(binding.root)






        binding.btnGoCommunity.setOnClickListener {
            findNavController().navigate(R.id.action_global_to_community)

        }


        binding.rvRecord.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecord.adapter = RecordAdapter(dummyList)

        binding.btnRecordCreate.setOnClickListener {
            findNavController().navigate(R.id.recordWriteFragment)
        }
    }
}
