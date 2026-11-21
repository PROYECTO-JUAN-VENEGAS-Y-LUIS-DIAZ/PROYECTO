package com.example.proyecto.domain.repository

import com.example.proyecto.domain.model.Shelter

interface ShelterRepository {
    suspend fun getShelters(): List<Shelter>
    suspend fun getShelterById(id: Long): Shelter?
    suspend fun addShelter(shelter: Shelter): Long
    suspend fun updateShelter(shelter: Shelter)
    suspend fun deleteShelter(id: Long)
}