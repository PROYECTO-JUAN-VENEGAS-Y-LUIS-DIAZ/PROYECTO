package com.example.proyecto.domain.repository

import com.example.proyecto.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observePets(): Flow<List<Pet>>
    suspend fun getPetById(id: Long): Pet?
    suspend fun addPet(pet: Pet): Long
    suspend fun updatePet(pet: Pet)
    suspend fun deletePet(id: Long)
}