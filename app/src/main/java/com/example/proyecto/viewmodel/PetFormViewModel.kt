package com.example.proyecto.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.AdoptionStatus
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.PetSize
import com.example.proyecto.domain.model.Species
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.domain.repository.ShelterRepository
import kotlinx.coroutines.launch

enum class PetFormError { NAME, SPECIES, AGE, SIZE, DESCRIPTION }

data class PetFormUiState(
    val name: String = "",
    val species: Species? = null,
    val ageText: String = "",
    val size: PetSize? = null,
    val breed: String = "",
    val description: String = "",
    val photoUri: String? = null,
    val errors: Set<PetFormError> = emptySet(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)
class PetFormViewModel(
    private val petRepository: PetRepository,
    private val shelterRepository: ShelterRepository,
    private val petId: Long? = null
) : ViewModel() {

    private val _uiState = MutableLiveData(PetFormUiState(isEditing = petId != null))
    val uiState: LiveData<PetFormUiState> = _uiState

    private var loadedPet: Pet? = null

    init {
        if (petId != null) loadPet(petId)
    }

    private fun loadPet(id: Long) {
        viewModelScope.launch {
            val pet = petRepository.getPetById(id) ?: return@launch
            loadedPet = pet
            update {
                it.copy(
                    name = pet.name,
                    species = pet.species,
                    ageText = pet.ageYears.toString(),
                    size = pet.size,
                    breed = pet.breed,
                    description = pet.description,
                    photoUri = pet.photoUri
                )
            }
        }
    }

    fun onNameChange(value: String) =
        update { it.copy(name = value, errors = it.errors - PetFormError.NAME) }

    fun onSpeciesChange(value: Species) =
        update { it.copy(species = value, errors = it.errors - PetFormError.SPECIES) }

    fun onAgeChange(value: String) =
        update { it.copy(ageText = value, errors = it.errors - PetFormError.AGE) }

    fun onSizeChange(value: PetSize) =
        update { it.copy(size = value, errors = it.errors - PetFormError.SIZE) }

    fun onBreedChange(value: String) = update { it.copy(breed = value) }

    fun onDescriptionChange(value: String) =
        update { it.copy(description = value, errors = it.errors - PetFormError.DESCRIPTION) }

    fun onPhotoSelected(uri: String?) = update { it.copy(photoUri = uri) }

    fun onSaveClick() {
        val state = _uiState.value ?: return
        val age = state.ageText.trim().toIntOrNull()?.takeIf { it in MIN_AGE..MAX_AGE }

        val errors = buildSet {
            if (state.name.trim().length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) add(PetFormError.NAME)
            if (state.species == null) add(PetFormError.SPECIES)
            if (age == null) add(PetFormError.AGE)
            if (state.size == null) add(PetFormError.SIZE)
            if (state.description.trim().length < MIN_DESCRIPTION_LENGTH) add(PetFormError.DESCRIPTION)
        }
        if (errors.isNotEmpty()) {
            update { it.copy(errors = errors) }
            return
        }

        update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val existing = loadedPet
            val shelterId = existing?.shelterId
                ?: shelterRepository.getShelters().firstOrNull()?.id
                ?: 0L
            val pet = Pet(
                id = existing?.id ?: 0L,
                name = state.name.trim(),
                species = requireNotNull(state.species),
                ageYears = requireNotNull(age),
                size = requireNotNull(state.size),
                breed = state.breed.trim(),
                description = state.description.trim(),
                photoUri = state.photoUri,
                status = existing?.status ?: AdoptionStatus.AVAILABLE,
                shelterId = shelterId
            )
            if (existing == null) petRepository.addPet(pet) else petRepository.updatePet(pet)
            update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun update(transform: (PetFormUiState) -> PetFormUiState) {
        _uiState.value = transform(_uiState.value ?: PetFormUiState())
    }

    private companion object {
        const val MIN_NAME_LENGTH = 2
        const val MAX_NAME_LENGTH = 30
        const val MIN_DESCRIPTION_LENGTH = 10
        const val MIN_AGE = 0
        const val MAX_AGE = 30
    }
}