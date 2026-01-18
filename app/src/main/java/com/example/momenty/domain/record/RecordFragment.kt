package com.example.momenty.domain.record

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentRecordBinding

class RecordFragment : Fragment(R.layout.fragment_record) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRecordBinding.bind(view)


        val dummyList = listOf(
            RecordItem("2025.11.21", "즐거운 날", "즐거움", "오늘은 특별한 날이었어요!"),
            RecordItem("2025.10.31", "슬픔", "내용 작성~~~", "내용 작성~~~"),
            RecordItem("2025.11.21", "즐거운 날", "즐거움", "오늘은 특별한 날이었어요!"),
            RecordItem("2025.10.31", "슬픔", "내용 작성~~~", "내용 작성~~~")
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, top, v.paddingRight, v.paddingBottom)
            insets
        }



        binding.rvRecord.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecord.adapter = RecordAdapter(dummyList)

        binding.btnRecordWrite.setOnClickListener {
            findNavController().navigate(R.id.action_recordFragment_to_writeFragment)
        }
    }
}
