package com.example.momenty.domain.mypage

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.global.security.LocalDataManager
import kotlinx.coroutines.launch

class MyPageViewModel(private val repository: MyPageRepository): ViewModel() {

    private var localDataManager: LocalDataManager? = null
    fun setLocalDataManager(manager: LocalDataManager) {
        this.localDataManager = manager
    }


    private val _addPetProfileResult = MutableLiveData<Result<String?>>()
    val addPetProfileResult: LiveData<Result<String?>> = _addPetProfileResult

    private val _loadOnePetProfileResult = MutableLiveData<Result<LoadOnePetProfileData>>()
    val loadOnePetProfileResult: LiveData<Result<LoadOnePetProfileData>> = _loadOnePetProfileResult

    private val _updatePetProfileResult = MutableLiveData<Result<String?>>()
    val updatePetProfileResult: LiveData<Result<String?>> = _updatePetProfileResult

    private val _loadPetListResult = MutableLiveData<Result<ArrayList<LoadPetListData>>>()
    val loadPetListResult: LiveData<Result<ArrayList<LoadPetListData>>> = _loadPetListResult



    private val _loadPetsProfileResult = MutableLiveData<Result<ArrayList<LoadOnePetProfileData>>>()
    val loadPetsProfileResult: LiveData<Result<ArrayList<LoadOnePetProfileData>>> = _loadPetsProfileResult



    private val _updateUserProfileResult = MutableLiveData<Result<UpdateUserProfileData>>()
    val updateUserProfileResult: LiveData<Result<UpdateUserProfileData>> = _updateUserProfileResult

    private val _loadProfileResult = MutableLiveData<Result<LoadProfileData>>()
    val loadProfileResult: LiveData<Result<LoadProfileData>> = _loadProfileResult

    private val _loadProfileDetailResult = MutableLiveData<Result<LoadProfileDetailData>>()
    val loadProfileDetailResult: LiveData<Result<LoadProfileDetailData>> = _loadProfileDetailResult



    private val _loadNoticeResult = MutableLiveData<Result<LoadNoticeData<_NoticeDatas, _PageInfoData>>>()
    val loadNoticeResult: LiveData<Result<LoadNoticeData<_NoticeDatas, _PageInfoData>>> = _loadNoticeResult

    private val _loadNoticeDetailResult = MutableLiveData<Result<LoadNoticeDetailData>>()
    val loadNoticeDetailResult: LiveData<Result<LoadNoticeDetailData>> = _loadNoticeDetailResult

    private val _addInquiryResult = MutableLiveData<Result<String?>>()
    val addInquiryResult: LiveData<Result<String?>> = _addInquiryResult

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


    private val _logoutResult = MutableLiveData<Result<String?>>()
    val logoutResult: LiveData<Result<String?>> = _logoutResult

    // ===================== pet-controller =====================

    /**
     * 반려동물 프로필 추가
     */
    fun addPetProfile(req: AddPetProfileRequest) {
        viewModelScope.launch {
            val result = repository.addPetProfile(req)
            _addPetProfileResult.postValue(result)
        }
    }

    /**
     * 특정 반려동물 프로필 조회
     */
    fun loadOnePetProfile(petId: Long) {
        viewModelScope.launch {
            val result = repository.loadOnePetProfile(petId)
            _loadOnePetProfileResult.postValue(result)
        }
    }

    /**
     * 반려동물 프로필 수정
     */
    fun updatePetProfile(petId: Long, req: UpdatePetProfileRequest) {
        viewModelScope.launch {
            val result = repository.updatePetProfile(petId, req)
            _updatePetProfileResult.postValue(result)
        }
    }

    /**
     * 내 반려동물 리스트 조회
     */
    fun loadPetList() {
        viewModelScope.launch {
            val result = repository.loadPetList()
            _loadPetListResult.postValue(result)
        }
    }



    // ===================== inquiry-controller =====================

    /**
     * 문의하기
     */
    fun addInquiry(type: String, content: String, imageUris: ArrayList<Uri>?, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val result = repository.addInquiry(type, content, imageUris, contentResolver)
            _addInquiryResult.postValue(result)
        }
    }

    /**
     * 문의하기 이미지 업로드용 Presigned URL 발급
     */
    fun createInquiryPresignedUrls(req: GetImageUrlRequest) {
        viewModelScope.launch {
            val result = repository.createInquiryPresignedUrls(req)
            _getImageUrlResult.postValue(result)
        }
    }

    /**
     * 문의내역 상세 조회
     */
    fun loadInquiryDetail(inquiryId: Long) {
        viewModelScope.launch {
            val result = repository.loadInquiryDetail(inquiryId)
            _loadInquiryDetailResult.postValue(result)
        }
    }

    /**
     * 문의내역 리스트 조회
     */
    fun loadInquiry(page: Int = 0,
                    size: Int = 10,
                    sort: ArrayList<String>
                    = ArrayList<String>().apply {
                        add("createdAt")
                        add("DESC")
                    }
    ) {
        viewModelScope.launch {
            val result = repository.loadInquiry(page, size, sort)
            _loadInquiryResult.postValue(result)
        }
    }



    // ===================== user-controller =====================

    /**
     * 사용자 프로필 조회
     */
    fun loadProfile() {
        viewModelScope.launch {
            val result = repository.loadProfile()
            _loadProfileResult.postValue(result)
        }
    }

    /**
     * 사용자 프로필 수정
     */
    fun updateUserProfile(req: UpdateUserProfileRequest) {
        viewModelScope.launch {
            val result = repository.updateUserProfile(req)
            _updateUserProfileResult.postValue(result)
        }
    }

    /**
     * 사용자 상세 조회
     */
    fun loadProfileDetail() {
        viewModelScope.launch {
            val result = repository.loadProfileDetail()
            _loadProfileDetailResult.postValue(result)
        }
    }



    // ===================== notice-controller =====================

    /**
     * 공지사항 리스트 조회
     */
    fun loadNotice(
        page: Int = 0,
        size: Int = 10,
        sort: ArrayList<String> = ArrayList<String>().apply {
            add("createdAt")
            add("DESC")
        }
    ) {
        viewModelScope.launch {
            val result = repository.loadNotice(page, size, sort)
            _loadNoticeResult.postValue(result)
        }
    }

    /**
     * 공지사항 상세 조회
     */
    fun loadNoticeDetail(noticeId: Long) {
        viewModelScope.launch {
            val result = repository.loadNoticeDetail(noticeId)
            _loadNoticeDetailResult.postValue(result)
        }
    }



    // ===================== faq-controller =====================

    /**
     * FAQ 리스트 조회
     */
    fun loadFaq() {
        viewModelScope.launch {
            val result = repository.loadFaq()
            _loadFaqResult.postValue(result)
        }
    }

    fun loadFaqDetail(faqId: Long) {
        viewModelScope.launch {
            val result = repository.loadFaqDetail(faqId)
            _loadFaqDetailResult.postValue(result)
        }
    }



    // ===================== UNKNOWN =====================

    /*
    fun loadPetsProfile() {
        viewModelScope.launch {
            val result = repository.loadPetsProfile()
            _loadPetsProfileResult.postValue(result)
        }
    }*/

    fun logout() {
        viewModelScope.launch {
            val result = repository.logout()
            _logoutResult.postValue(result)
        }
    }
}