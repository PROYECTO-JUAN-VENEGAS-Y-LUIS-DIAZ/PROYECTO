package com.example.proyecto.domain.model

data class Shelter(
    val id: Long,
    val name: String,
    val phone: String,
    val address: String,
    val location: GeoPoint
)