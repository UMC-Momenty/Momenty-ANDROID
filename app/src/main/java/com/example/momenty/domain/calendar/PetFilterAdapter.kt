package com.example.momenty.domain.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.momenty.R

class PetFilterAdapter(
    private var pets: List<Pet>,
    private val multiSelect: Boolean = false, // true: 다중 선택, false: 단일 선택
    private val onPetClick: (Pet?) -> Unit
) : RecyclerView.Adapter<PetFilterAdapter.PetViewHolder>() {

    // 단일 선택 모드용
    private var selectedPosition: Int? = null

    // 다중 선택 모드용
    private val selectedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_filter, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val isSelected = if (multiSelect) {
            selectedPositions.contains(position)
        } else {
            position == selectedPosition
        }
        holder.bind(pets[position], isSelected)
    }

    override fun getItemCount(): Int = pets.size

    /**
     * 펫 목록 업데이트
     */
    fun updatePets(newPets: List<Pet>) {
        pets = newPets

        if (multiSelect) {
            selectedPositions.clear()
        } else {
            selectedPosition = null
        }

        notifyDataSetChanged()
    }

    /**
     * 단일 선택 모드에서 펫 선택
     */
    fun selectPet(pet: Pet?) {
        if (multiSelect) return // 다중 선택 모드에서는 동작하지 않음

        val position = if (pet == null) null else pets.indexOf(pet)
        if (position != selectedPosition) {
            val previousPosition = selectedPosition
            selectedPosition = position
            previousPosition?.let { notifyItemChanged(it) }
            position?.let { notifyItemChanged(it) }
        }
    }

    /**
     * 다중 선택 모드에서 선택된 펫 목록 가져오기
     */
    fun getSelectedPets(): List<Pet> {
        if (!multiSelect) return emptyList()
        return selectedPositions.mapNotNull { position ->
            pets.getOrNull(position)
        }
    }

    /**
     * 다중 선택 모드에서 모든 선택 해제
     */
    fun clearSelection() {
        if (!multiSelect) return

        val previousPositions = selectedPositions.toSet()
        selectedPositions.clear()
        previousPositions.forEach { notifyItemChanged(it) }
    }

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val petImage: ImageView = itemView.findViewById(R.id.iv_pet_image)
        private val selectionBorder: View = itemView.findViewById(R.id.view_selection_border)

        fun bind(pet: Pet, isSelected: Boolean) {
            // 선택 상태 표시
            selectionBorder.visibility = if (isSelected) View.VISIBLE else View.GONE

            // 이미지 로드
            if (pet.imageUrl.isNullOrEmpty()) {
                // 펫 이미지 없을 경우 회색 원 표시 (개발용)
                val grayCircle = ContextCompat.getDrawable(itemView.context, R.drawable.ic_pet_placeholder)
                petImage.setImageDrawable(grayCircle)
            } else {
                Glide.with(itemView.context)
                    .load(pet.imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_placeholder)
                    .into(petImage)
            }

            // 클릭 리스너
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    handleClick(adapterPosition, pet)
                }
            }
        }

        private fun handleClick(position: Int, pet: Pet) {
            if (multiSelect) {
                // 다중 선택 모드
                if (selectedPositions.contains(position)) {
                    // 이미 선택된 경우 선택 해제
                    selectedPositions.remove(position)
                    notifyItemChanged(position)
                    onPetClick(pet) // 선택 해제 알림
                } else {
                    // 선택되지 않은 경우 선택 추가
                    selectedPositions.add(position)
                    notifyItemChanged(position)
                    onPetClick(pet) // 선택 알림
                }
            } else {
                // 단일 선택 모드
                val previousPosition = selectedPosition

                // 같은 항목을 다시 클릭하면 선택 해제 (전체로 전환)
                if (selectedPosition == position) {
                    selectedPosition = null
                    notifyItemChanged(position)
                    onPetClick(null) // 전체 일정
                }
                // 다른 항목 선택
                else {
                    selectedPosition = position
                    previousPosition?.let { notifyItemChanged(it) }
                    notifyItemChanged(position)
                    onPetClick(pet)
                }
            }
        }
    }
}