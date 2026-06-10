package com.example.tvmaze.data.model

import com.google.gson.annotations.SerializedName

data class Show(
    val id: Int,
    val name: String,
    val summary: String?,
    val genres: List<String>,
    val rating: Rating?,
    val image: Image?,
    val language: String?,
    val premiered: String?,
    val ended: String?,
    val status: String?,
    val officialSite: String?
)

data class Rating(
    val average: Double?
)

data class Image(
    val medium: String?,
    val original: String?
)