package com.example.proyecto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.domain.repository.ShelterRepository

class AdoptaPetViewModelFactory(
    private val petRepository: PetRepository,
    private val shelterRepository: ShelterRepository,
    private val petId: Long? = null
) : ViewModelProvider.Factory {

    fun withPetId(petId: Long?) =
        AdoptaPetViewModelFactory(petRepository, shelterRepository, petId)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(PetListViewModel::class.java) ->
            PetListViewModel(petRepository)

        modelClass.isAssignableFrom(PetDetailViewModel::class.java) ->
            PetDetailViewModel(
                petRepository,
                shelterRepository,
                requireNotNull(petId) { "petId is required for PetDetailViewModel" }
            )

        modelClass.isAssignableFrom(PetFormViewModel::class.java) ->
            PetFormViewModel(petRepository, shelterRepository, petId)

        modelClass.isAssignableFrom(ShelterMapViewModel::class.java) ->
            ShelterMapViewModel(shelterRepository)

        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    } as T
}