package com.example.proyecto.domain.repository

import com.example.proyecto.domain.model.Shelter

interface ShelterRepository {
    suspend fun getShelters(): List<Shelter>
    suspend fun getShelterById(id: Long): Shelter?
}