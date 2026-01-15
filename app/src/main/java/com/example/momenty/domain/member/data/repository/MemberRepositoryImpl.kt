package com.example.momenty.domain.member.data.repository


import com.example.momenty.domain.member.data.api.MemberApi
import com.example.momenty.domain.member.domain.repository.MemberRepository
import com.example.momenty.global.security.TokenManager
import javax.inject.Inject

class MemberRepositoryImpl @Inject constructor(
    private val memberApi: MemberApi,
    private val tokenManager: TokenManager
) : MemberRepository