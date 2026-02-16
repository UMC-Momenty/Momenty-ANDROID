package com.example.momenty.domain.mypage.RVA

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.databinding.ItemMyPagePetSelectBinding
import com.example.momenty.domain.mypage.PetFormManageActivity
import com.example.momenty.domain.mypage.data.MyPagePetProfileData

class PetSelectBottomSheetRVA(
    private var petData: ArrayList<MyPagePetProfileData>,
    private val onDismiss: () -> Unit
) : RecyclerView.Adapter<PetSelectBottomSheetRVA.PetSelectViewHolder>() {

    private val TAG = "PetBottomRVA"

    inner class PetSelectViewHolder(val binding: ItemMyPagePetSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: MyPagePetProfileData) {
            binding.tvMyPagePetSelectName.text = data.name

            if (!data.imageKey.isNullOrEmpty()) {
                Log.e(TAG, "imageKey 시도")
                Glide.with(binding.root.context)
                    .load(data.imageKey)
                    .circleCrop()
                    .error(R.drawable.bg_calendar_selected)
                    .into(binding.ivMyPagePetSelect)
            } else if (!data.imageUri.isNullOrEmpty()) {
                Log.e(TAG, "imageUri 시도: ${data.imageUri}")
                Glide.with(binding.root.context)
                    .load(Uri.parse(data.imageUri))
                    .circleCrop()
                    .error(R.drawable.bg_calendar_selected)
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
        holder.binding.rbMyPagePetSelect.setOnClickListener {
            val intent = Intent(holder.itemView.context, PetFormManageActivity::class.java)
            intent.putExtra("petIndex", position)
            Log.e("PetSelectRVA", "position: $position")
            holder.itemView.context.startActivity(intent)
            holder.binding.rbMyPagePetSelect.setChecked(false)
            onDismiss.invoke()
        }
    }

    override fun getItemCount(): Int = petData.size
}