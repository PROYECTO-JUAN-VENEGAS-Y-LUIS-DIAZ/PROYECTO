package com.example.proyecto.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.R
import com.example.proyecto.domain.model.AdoptionStatus
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.PetSize
import com.example.proyecto.domain.model.Species
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.util.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetFormState(
    val id: Long = 0L,
    val isEditing: Boolean = false,
    val name: String = "",
    val species: Species? = null,
    val age: String = "",
    val size: PetSize? = null,
    val breed: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val status: AdoptionStatus = AdoptionStatus.AVAILABLE,
    val shelterId: Long = AppConstants.DEFAULT_SHELTER_ID,
    @StringRes val nameError: Int? = null,
    @StringRes val speciesError: Int? = null,
    @StringRes val ageError: Int? = null,
    @StringRes val sizeError: Int? = null,
    @StringRes val descriptionError: Int? = null,
    @StringRes val errorRes: Int? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

class PetFormViewModel(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PetFormState())
    val state: StateFlow<PetFormState> = _state.asStateFlow()

    /** Call when the form is opened to edit an existing pet. */
    fun loadPet(petId: Long) {
        viewModelScope.launch {
            val pet = petRepository.getPetById(petId)
            if (pet == null) {
                _state.update { it.copy(errorRes = R.string.error_pet_not_found) }
            } else {
                _state.value = PetFormState(
                    id = pet.id,
                    isEditing = true,
                    name = pet.name,
                    species = pet.species,
                    age = pet.ageYears.toString(),
                    size = pet.size,
                    breed = pet.breed,
                    description = pet.description,
                    photoUri = pet.photoUri,
                    status = pet.status,
                    shelterId = pet.shelterId
                )
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = null) }
    fun onSpeciesChange(value: Species) = _state.update { it.copy(species = value, speciesError = null) }
    fun onAgeChange(value: String) = _state.update { it.copy(age = value.filter(Char::isDigit), ageError = null) }
    fun onSizeChange(value: PetSize) = _state.update { it.copy(size = value, sizeError = null) }
    fun onBreedChange(value: String) = _state.update { it.copy(breed = value) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value, descriptionError = null) }
    fun onPhotoSelected(uri: String?) = _state.update { it.copy(photoUri = uri) }
    fun onErrorShown() = _state.update { it.copy(errorRes = null) }

    fun save() {
        val current = _state.value
        val species = current.species
        val size = current.size
        val age = current.age.trim().toIntOrNull()

        val nameError = if (current.name.isBlank()) R.string.error_required else null
        val speciesError = if (species == null) R.string.error_required else null
        val sizeError = if (size == null) R.string.error_required else null
        val descriptionError = if (current.description.isBlank()) R.string.error_required else null
        val ageError = when {
            current.age.isBlank() -> R.string.error_required
            age == null || age !in 0..AppConstants.MAX_PET_AGE -> R.string.error_age_invalid
            else -> null
        }

        val hasErrors = listOf(nameError, speciesError, sizeError, descriptionError, ageError)
            .any { it != null }

        if (hasErrors || species == null || size == null || age == null) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    speciesError = speciesError,
                    ageError = ageError,
                    sizeError = sizeError,
                    descriptionError = descriptionError
                )
            }
            return
        }

        val pet = Pet(
            id = current.id,
            name = current.name.trim(),
            species = species,
            ageYears = age,
            size = size,
            breed = current.breed.trim(),
            description = current.description.trim(),
            photoUri = current.photoUri,
            status = current.status,
            shelterId = current.shelterId
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                if (current.isEditing) petRepository.updatePet(pet) else petRepository.addPet(pet)
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, errorRes = R.string.error_generic) }
            }
        }
    }
}