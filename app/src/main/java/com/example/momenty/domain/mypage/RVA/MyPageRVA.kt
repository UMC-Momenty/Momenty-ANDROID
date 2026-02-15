package com.example.momenty.domain.mypage.RVA

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.databinding.ItemMyPagePetProfileBinding
import com.example.momenty.databinding.ItemNoticeBinding
import com.example.momenty.domain.mypage.data.MyPagePetProfileData
import com.example.momenty.domain.mypage.data.NoticeData
import com.google.android.material.bottomsheet.BottomSheetDialog

class MyPageRVA(private val petProfileList: ArrayList<MyPagePetProfileData>,
    private val onButtonClick: (MyPagePetProfileData)->Unit)
    : RecyclerView.Adapter<MyPageRVA.viewHolder>() {

    private var bottomSheetDialog: BottomSheetDialog? = null


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
        holder.binding.btnMyPageManagePet.setOnClickListener {
//            val manager = (holder.itemView.context as? FragmentActivity)?.supportFragmentManager
//            bottomSheetDialog = BottomSheetDialog(holder.itemView.context, R.style.BottomSheetStyle)
//            val view = LayoutInflater.from(holder.itemView.context).in
        }
    }

    override fun getItemCount(): Int {
        return petProfileList.size
    }

    inner class viewHolder(val binding: ItemMyPagePetProfileBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MyPagePetProfileData) {
            binding.tvMyPagePetName.text = data.name
            //binding.ivMyPagePetProfile.setImageResource()
            if (!data.imageKey.isNullOrEmpty()) {
                val baseUrl = "https://api.momenty.com/"
                val imageUrl = baseUrl + data.imageKey

                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .circleCrop()
                    .into(binding.ivMyPagePetProfile)
            }

            binding.btnMyPageManagePet.setOnClickListener {
                onButtonClick(data)
            }
        }
    }
}