package com.example.proyecto.domain.model

data class Pet(
    val id: Long = 0L,
    val name: String,
    val species: Species,
    val ageYears: Int,
    val size: PetSize,
    val breed: String = "",
    val description: String,
    val photoUri: String? = null,
    val status: AdoptionStatus = AdoptionStatus.AVAILABLE,
    val shelterId: Long
)