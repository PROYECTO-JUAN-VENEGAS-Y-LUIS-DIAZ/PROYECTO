package com.example.proyecto.domain.repository

import com.example.proyecto.domain.model.Shelter
import kotlinx.coroutines.flow.Flow

interface ShelterRepository {
    fun observeShelters(): Flow<List<Shelter>>
    suspend fun getShelterById(id: Long): Shelter?
}