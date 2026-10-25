package com.example.proyecto.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.R
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.domain.repository.ShelterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetDetailUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val shelter: Shelter? = null,
    val showDeleteDialog: Boolean = false,
    val deleted: Boolean = false,
    @StringRes val errorRes: Int? = null
)

class PetDetailViewModel(
    private val petRepository: PetRepository,
    private val shelterRepository: ShelterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetDetailUiState())
    val uiState: StateFlow<PetDetailUiState> = _uiState.asStateFlow()

    fun load(petId: Long) {
        viewModelScope.launch {
            val pet = petRepository.getPetById(petId)
            if (pet == null) {
                _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_pet_not_found) }
            } else {
                val shelter = shelterRepository.getShelterById(pet.shelterId)
                _uiState.update { it.copy(isLoading = false, pet = pet, shelter = shelter, errorRes = null) }
            }
        }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteConfirm() {
        val pet = _uiState.value.pet ?: return
        viewModelScope.launch {
            try {
                petRepository.deletePet(pet.id)
                _uiState.update { it.copy(showDeleteDialog = false, deleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(showDeleteDialog = false, errorRes = R.string.error_generic) }
            }
        }
    }
}