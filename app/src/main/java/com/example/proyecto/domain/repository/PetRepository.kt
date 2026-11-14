package com.example.proyecto.domain.repository

import com.example.proyecto.domain.model.Pet

interface PetRepository {
    suspend fun getPets(): List<Pet>
    suspend fun getPetById(id: Long): Pet?
    suspend fun addPet(pet: Pet): Long
    suspend fun updatePet(pet: Pet)
    suspend fun deletePet(id: Long)
}
