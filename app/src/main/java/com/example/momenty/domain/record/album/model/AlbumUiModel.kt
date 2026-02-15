package com.example.momenty.domain.record.album.model

data class AlbumUiModel(
    val id: String,
    val title: String,
    val count: Int,
    val thumbRes: Int,
    val type: AlbumType,
)

enum class AlbumType { DOG, CAT }
