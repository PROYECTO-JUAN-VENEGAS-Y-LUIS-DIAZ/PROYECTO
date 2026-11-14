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
import kotlinx.coroutines.launch

data class PetListUiState(
    val pets: List<Pet> = emptyList(),
    val query: String = "",
    val species: Set<Species> = emptySet(),
    val sizes: Set<PetSize> = emptySet(),
    val statuses: Set<AdoptionStatus> = emptySet(),
    val breed: String = "",
    val isLoading: Boolean = true,
    val petPendingDelete: Pet? = null
)

class PetListViewModel(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(PetListUiState())
    val uiState: LiveData<PetListUiState> = _uiState

    private var allPets: List<Pet> = emptyList()

    init {
        loadPets()
    }

    fun loadPets() {
        viewModelScope.launch {
            allPets = petRepository.getPets()
            publish { it.copy(isLoading = false) }
        }
    }

    fun onQueryChange(query: String) {
        publish { it.copy(query = query) }
    }

    fun onSpeciesChipSelected(species: Species?) {
        publish { it.copy(species = species?.let { s -> setOf(s) } ?: emptySet()) }
    }

    fun onApplyFilters(
        species: Set<Species>,
        sizes: Set<PetSize>,
        statuses: Set<AdoptionStatus>,
        breed: String
    ) {
        publish { it.copy(species = species, sizes = sizes, statuses = statuses, breed = breed) }
    }

    fun onClearFilters() {
        publish {
            it.copy(
                species = emptySet(),
                sizes = emptySet(),
                statuses = emptySet(),
                breed = ""
            )
        }
    }

    fun onDeleteRequested(pet: Pet) {
        publish { it.copy(petPendingDelete = pet) }
    }

    fun onDeleteDismissed() {
        publish { it.copy(petPendingDelete = null) }
    }

    fun onDeleteConfirmed() {
        val pet = _uiState.value?.petPendingDelete ?: return
        viewModelScope.launch {
            petRepository.deletePet(pet.id)
            allPets = petRepository.getPets()
            publish { it.copy(petPendingDelete = null) }
        }
    }

    private fun publish(transform: (PetListUiState) -> PetListUiState) {
        val updated = transform(_uiState.value ?: PetListUiState())
        _uiState.value = updated.copy(pets = allPets.filter { matches(it, updated) })
    }

    private fun matches(pet: Pet, state: PetListUiState): Boolean {
        val text = state.query.trim()
        val breed = state.breed.trim()
        return (text.isEmpty() || pet.name.contains(text, ignoreCase = true)) &&
                (state.species.isEmpty() || pet.species in state.species) &&
                (state.sizes.isEmpty() || pet.size in state.sizes) &&
                (state.statuses.isEmpty() || pet.status in state.statuses) &&
                (breed.isEmpty() || pet.breed.contains(breed, ignoreCase = true))
    }
}