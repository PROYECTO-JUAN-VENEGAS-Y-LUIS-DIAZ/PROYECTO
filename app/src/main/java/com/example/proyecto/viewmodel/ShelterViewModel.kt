package com.example.proyecto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proyecto.AdoptaPetApplication
import com.example.proyecto.domain.model.Shelter

class ShelterViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = (application as AdoptaPetApplication).repository

    suspend fun getShelters(): List<Shelter> {
        return repository.getShelters()
    }

    suspend fun getShelterById(id: Long): Shelter? {
        return repository.getShelterById(id)
    }

    private fun validateLocation(shelter: Shelter) {
        if (shelter.location.latitude < -90.0 || shelter.location.latitude > 90.0)
            throw Exception("The latitude must be between -90 and 90.")

        if (shelter.location.longitude < -180.0 || shelter.location.longitude > 180.0)
            throw Exception("The longitude must be between -180 and 180.")
    }

    suspend fun saveShelter(shelter: Shelter): Long {
        validateLocation(shelter)

        val isDuplicated = repository.getShelters().any {
            it.name.trim().equals(shelter.name.trim(), ignoreCase = true)
        }
        if (isDuplicated)
            throw Exception("The shelter is duplicated. Please add a new one.")

        return repository.addShelter(shelter)
    }

    suspend fun updateShelter(shelter: Shelter) {
        validateLocation(shelter)

        if (repository.getShelterById(shelter.id) != null)
            repository.updateShelter(shelter)
        else
            throw Exception("It's not possible to update the shelter. because it does not exist.")
    }

    suspend fun removeShelter(id: Long) {
        if (repository.getShelterById(id) == null)
            throw Exception("It's not possible to delete the shelter. because it does not exist.")

        if (repository.getPets().any { it.shelterId == id })
            throw Exception("It's not possible to delete the shelter. because it has pets assigned.")

        repository.deleteShelter(id)
    }

    class Factory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShelterViewModel::class.java)) {
                return ShelterViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
