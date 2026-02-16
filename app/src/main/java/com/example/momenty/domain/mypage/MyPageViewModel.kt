package com.example.momenty.domain.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.domain.home.LoadQuestData
import com.example.momenty.domain.home.QuestRepository
import com.example.momenty.domain.home.WriteQuestData
import com.example.momenty.domain.home.WriteQuestRequest
import kotlinx.coroutines.launch

class MyPageViewModel(private val repository: MyPageRepository): ViewModel() {
    private val _loadProfileResult = MutableLiveData<Result<LoadProfileData<PetProfileData>>>()
    val loadProfileResult: LiveData<Result<LoadProfileData<PetProfileData>>> = _loadProfileResult

    private val _updateUserProfileResult = MutableLiveData<Result<Unit>>()
    val updateUserProfileResult: LiveData<Result<Unit>> = _updateUserProfileResult

    private val _updatePetProfileResult = MutableLiveData<Result<Unit>>()
    val updatePetProfileResult: LiveData<Result<Unit>> = _updatePetProfileResult

    private val _addPetProfileResult = MutableLiveData<Result<Unit>>()
    val addPetProfileResult: LiveData<Result<Unit>> = _addPetProfileResult

    private val _loadNoticeResult = MutableLiveData<Result<LoadNoticeData<_NoticeData, _PageInfoData>>>()
    val loadNoticeResult: LiveData<Result<LoadNoticeData<_NoticeData, _PageInfoData>>> = _loadNoticeResult

    private val _loadNoticeDetailResult = MutableLiveData<Result<LoadNoticeDetailData>>()
    val loadNoticeDetailResult: LiveData<Result<LoadNoticeDetailData>> = _loadNoticeDetailResult

    private val _addInquiryResult = MutableLiveData<Result<String>>()
    val addInquiryResult: LiveData<Result<String>> = _addInquiryResult

    private val _getImageUrlResult = MutableLiveData<Result<ArrayList<GetImageUrlData>>>()
    val getImageUrlResult: LiveData<Result<ArrayList<GetImageUrlData>>> = _getImageUrlResult

    private val _loadInquiryResult = MutableLiveData<Result<LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo>>>()
    val loadInquiryResult: LiveData<Result<LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo>>> = _loadInquiryResult

    private val _loadInquiryDetailResult = MutableLiveData<Result<LoadInquiryDetailData<LoadInquiryDetailDataImages>>>()
    val loadInquiryDetailResult: LiveData<Result<LoadInquiryDetailData<LoadInquiryDetailDataImages>>> = _loadInquiryDetailResult

    private val _loadFaqResult = MutableLiveData<Result<ArrayList<LoadFaqData>>>()
    val loadFaqResult: LiveData<Result<ArrayList<LoadFaqData>>> = _loadFaqResult

    private val _loadFaqDetailResult = MutableLiveData<Result<LoadFaqDetailData>>()
    val loadFaqDetailResult: LiveData<Result<LoadFaqDetailData>> = _loadFaqDetailResult


    fun loadProfile(accessToken:String, userId: Long) {
        viewModelScope.launch {
            val result = repository.loadProfile(accessToken, userId)
            _loadProfileResult.postValue(result)
        }
    }

    fun updateUserProfile(accessToken: String, userId: Long, req: UpdateUserProfileRequest) {
        viewModelScope.launch {
            val result = repository.updateUserProfile(accessToken, userId, req)
            _updateUserProfileResult.postValue(result)
        }
    }

    fun updatePetProfile(accessToken: String, userId: Long, petId: Int, req: UpdatePetProfileRequest) {
        viewModelScope.launch {
            val result = repository.updatePetProfile(accessToken, userId, petId, req)
            _updatePetProfileResult.postValue(result)
        }
    }

    fun addPetProfile(accessToken: String, userId: Long, req: AddPetProfileRequest) {
        viewModelScope.launch {
            val result = repository.addPetProfile(accessToken, userId, req)
            _addPetProfileResult.postValue(result)
        }
    }

    fun loadNotice(accessToken: String) {
        viewModelScope.launch {
            val result = repository.loadNotice(accessToken)
            _loadNoticeResult.postValue(result)
        }
    }

    fun loadNoticeDetail(accessToken: String, noticeId: Int) {
        viewModelScope.launch {
            val result = repository.loadNoticeDetail(accessToken, noticeId)
            _loadNoticeDetailResult.postValue(result)
        }
    }

    fun addInquiry(accessToken: String, userId: Long, req: AddInquiryRequest<AddInquiryRequestImg>) {
        viewModelScope.launch {
            val result = repository.addInquiry(accessToken, userId, req)
            _addInquiryResult.postValue(result)
        }
    }

    fun getImageUrl(accessToken: String, req: GetImageUrlRequest) {
        viewModelScope.launch {
            val result = repository.getImageUrl(accessToken, req)
            _getImageUrlResult.postValue(result)
        }
    }

    fun loadInquiry(accessToken: String, userId: Long) {
        viewModelScope.launch {
            val result = repository.loadInquiry(accessToken, userId)
            _loadInquiryResult.postValue(result)
        }
    }

    fun loadInquiryDetail(accessToken: String, inquiryId: Int) {
        viewModelScope.launch {
            val result = repository.loadInquiryDetail(accessToken, inquiryId)
            _loadInquiryDetailResult.postValue(result)
        }
    }

    fun loadFaq(accessToken: String) {
        viewModelScope.launch {
            val result = repository.loadFaq(accessToken)
            _loadFaqResult.postValue(result)
        }
    }

    fun loadFaqDetail(accessToken: String, faqId: Int) {
        viewModelScope.launch {
            val result = repository.loadFaqDetail(accessToken, faqId)
            _loadFaqDetailResult.postValue(result)
        }
    }
}