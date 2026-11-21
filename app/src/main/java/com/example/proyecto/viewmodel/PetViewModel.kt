package com.example.proyecto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.AdoptaPetApplication
import com.example.proyecto.domain.model.AdoptionStatus
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.Shelter

class PetViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = (application as AdoptaPetApplication).repository

    suspend fun getPets(): List<Pet> {
        return repository.getPets()
    }

    suspend fun getPetById(id: Long): Pet? {
        return repository.getPetById(id)
    }

    suspend fun getShelterById(id: Long): Shelter? {
        return repository.getShelterById(id)
    }

    suspend fun savePet(pet: Pet): Long {
        if (pet.status != AdoptionStatus.AVAILABLE)
            throw Exception("A new pet must be created with AVAILABLE status.")

        val shelterId = repository.getShelters().firstOrNull()?.id
            ?: throw Exception("There are no shelters. Please register a shelter first.")

        return repository.addPet(pet.copy(shelterId = shelterId))
    }

    suspend fun updatePet(pet: Pet) {
        if (repository.getPetById(pet.id) != null)
            repository.updatePet(pet)
        else
            throw Exception("It's not possible to update the pet. because it does not exist.")
    }

    suspend fun removePet(id: Long) {
        if (repository.getPetById(id) != null)
            repository.deletePet(id)
        else
            throw Exception("It's not possible to delete the pet. because it does not exist.")
    }

    class Factory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
                return PetViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

