package com.example.momenty.domain.community.write

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.momenty.domain.community.CommunityCategory

class CommunityWriteViewModel : ViewModel() {

    val title = MutableLiveData("")
    val content = MutableLiveData("")

    private val _photos = MutableLiveData<List<Uri>>(emptyList())
    val photos: LiveData<List<Uri>> = _photos

    private val _category = MutableLiveData<CommunityCategory?>(null)
    val category: LiveData<CommunityCategory?> = _category

    val canSubmit = MediatorLiveData(false)

    val photoCountText = MediatorLiveData("0/10").apply {
        addSource(_photos) { list ->
            value = "${list.size}/10"
        }
    }


    init {
        canSubmit.addSource(title) { validate() }
        canSubmit.addSource(content) { validate() }
        canSubmit.addSource(_category) { validate() }

    }

    private fun validate() {
        canSubmit.value =
            !title.value.isNullOrBlank() &&
                    !content.value.isNullOrBlank() &&
                    _category.value != null
    }

    fun setCategory(cat: CommunityCategory) {
        _category.value = cat
    }

    fun addPhotos(newOnes: List<Uri>, max: Int = 10) {
        _photos.value = (_photos.value.orEmpty() + newOnes)
            .distinct()
            .take(max)
    }

    fun removePhoto(uri: Uri) {
        _photos.value = _photos.value.orEmpty().filterNot { it == uri }
    }
}
