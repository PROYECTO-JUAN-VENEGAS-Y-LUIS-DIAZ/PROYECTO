package com.example.proyecto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.domain.repository.ShelterRepository

class AdoptaPetViewModelFactory(
    private val petRepository: PetRepository,
    private val shelterRepository: ShelterRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(PetListViewModel::class.java) ->
            PetListViewModel(petRepository)
        modelClass.isAssignableFrom(PetDetailViewModel::class.java) ->
            PetDetailViewModel(petRepository, shelterRepository)
        modelClass.isAssignableFrom(PetFormViewModel::class.java) ->
            PetFormViewModel(petRepository)
        modelClass.isAssignableFrom(ShelterMapViewModel::class.java) ->
            ShelterMapViewModel(shelterRepository)
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    } as T
}