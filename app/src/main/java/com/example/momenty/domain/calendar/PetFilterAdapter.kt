package com.example.momenty.domain.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.momenty.R

class PetFilterAdapter(
    private var pets: List<Pet>, // null은 "전체" 옵션
    private val onPetClick: (Pet?) -> Unit
) : RecyclerView.Adapter<PetFilterAdapter.PetViewHolder>() {

    private var selectedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_filter, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(pets[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = pets.size

    fun updatePets(newPets: List<Pet>) {
        pets = newPets
        selectedPosition = null // 전체로 초기화
        notifyDataSetChanged()
    }

    fun selectPet(pet: Pet?) {
        val position = if(pet == null) null else pets.indexOf(pet)
        if (position != selectedPosition) {
            val previousPosition = selectedPosition
            selectedPosition = position
            previousPosition?.let { notifyItemChanged(it) }
            position?.let { notifyItemChanged(it) }
        }
    }

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petImage: ImageView = itemView.findViewById(R.id.iv_pet_image)
        private val selectionBorder: View = itemView.findViewById(R.id.view_selection_border)

        fun bind(pet: Pet, isSelected: Boolean) {
            // 선택 상태 표시
            selectionBorder.visibility = if (isSelected) View.VISIBLE else View.GONE

            // 이미지 로드
            Glide.with(itemView.context)
                .load(pet.imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_pet_placeholder) // 이미지 로딩 중 보일 임시 이미지(회색 원)
                .error(R.drawable.ic_pet_placeholder)
                .into(petImage)

            // 클릭 리스너 - 토글 방식
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val previousPosition = selectedPosition

                    // 같은 항목을 다시 클릭하면 선택 해제 (전체로 전환)
                    if (selectedPosition == adapterPosition) {
                        selectedPosition = null
                        notifyItemChanged(adapterPosition)
                        onPetClick(null) // 전체 일정
                    }
                    // 다른 항목 선택
                    else {
                        selectedPosition = adapterPosition
                        previousPosition?.let { notifyItemChanged(it) }
                        notifyItemChanged(selectedPosition!!)
                        onPetClick(pet)
                    }
                }
            }
        }
    }
}