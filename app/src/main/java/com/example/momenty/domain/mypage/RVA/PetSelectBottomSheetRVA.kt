package com.example.momenty.domain.mypage.RVA

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.databinding.ItemMyPagePetSelectBinding
import com.example.momenty.domain.mypage.data.MyPagePetProfileData

class PetSelectBottomSheetRVA(
    private var petData: ArrayList<MyPagePetProfileData>
) : RecyclerView.Adapter<PetSelectBottomSheetRVA.PetSelectViewHolder>() {

    inner class PetSelectViewHolder(private val binding: ItemMyPagePetSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: MyPagePetProfileData) {
            binding.tvMyPagePetSelectName.text = data.name

            if (!data.imageKey.isNullOrEmpty()) {
                val baseUrl = "https://api.momenty.com/"
                val imageUrl = baseUrl + data.imageKey

                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .circleCrop()
                    .into(binding.ivMyPagePetSelect)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetSelectViewHolder {
        val binding = ItemMyPagePetSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PetSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetSelectViewHolder, position: Int) {
        holder.bind(petData[position])
    }

    override fun getItemCount(): Int = petData.size
}