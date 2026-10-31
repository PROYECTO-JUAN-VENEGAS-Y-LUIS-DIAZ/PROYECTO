package com.example.proyecto.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.domain.repository.ShelterRepository
import kotlinx.coroutines.launch

data class PetDetailUiState(
    val pet: Pet? = null,
    val shelter: Shelter? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleted: Boolean = false
)

class PetDetailViewModel(
    private val petRepository: PetRepository,
    private val shelterRepository: ShelterRepository,
    private val petId: Long
) : ViewModel() {

    private val _uiState = MutableLiveData(PetDetailUiState())
    val uiState: LiveData<PetDetailUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val pet = petRepository.getPetById(petId)
            val shelter = pet?.let { shelterRepository.getShelterById(it.shelterId) }
            update { it.copy(pet = pet, shelter = shelter, isLoading = false, notFound = pet == null) }
        }
    }

    fun onDeleteClick() {
        update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismissed() {
        update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            petRepository.deletePet(petId)
            update { it.copy(showDeleteDialog = false, isDeleted = true) }
        }
    }

    private fun update(transform: (PetDetailUiState) -> PetDetailUiState) {
        _uiState.value = transform(_uiState.value ?: PetDetailUiState())
    }
}