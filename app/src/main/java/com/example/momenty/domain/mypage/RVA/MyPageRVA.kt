package com.example.momenty.domain.mypage.RVA

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.databinding.ItemMyPagePetProfileBinding
import com.example.momenty.databinding.ItemNoticeBinding
import com.example.momenty.domain.mypage.LoadPetListData
import com.example.momenty.domain.mypage.data.MyPagePetProfileData
import com.example.momenty.domain.mypage.data.NoticeData
import com.google.android.material.bottomsheet.BottomSheetDialog

class MyPageRVA(private val petProfileList: ArrayList<LoadPetListData>,
    private val onButtonClick: (ArrayList<LoadPetListData>)->Unit)
    : RecyclerView.Adapter<MyPageRVA.viewHolder>() {


        private val TAG = "MyPageRVA"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding: ItemMyPagePetProfileBinding = ItemMyPagePetProfileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(petProfileList[position])
        if (position == 0) {
            holder.binding.btnMyPageManagePet.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return petProfileList.size
    }

    inner class viewHolder(val binding: ItemMyPagePetProfileBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: LoadPetListData) {
            binding.tvMyPagePetName.text = data.petName
            //binding.ivMyPagePetProfile.setImageResource()
            if (!data.profileImageUrl.isNullOrEmpty()) {
                Log.e(TAG, "try imageKey")
                Glide.with(binding.root.context)
                    .load(data.profileImageUrl)
                    .circleCrop()
                    .into(binding.ivMyPagePetProfile)
            }/* else if (!data.imageUri.isNullOrEmpty()) {
                Log.e(TAG, "try imageUri")
                Glide.with(binding.root.context)
                    .load(data.imageUri.toUri())
                    .circleCrop()
                    .into(binding.ivMyPagePetProfile)
            }*/

            binding.btnMyPageManagePet.setOnClickListener {
                onButtonClick(petProfileList)
            }
        }
    }
}