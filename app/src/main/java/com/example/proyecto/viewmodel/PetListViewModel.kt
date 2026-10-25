package com.example.proyecto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.R
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.PetFilter
import com.example.proyecto.domain.model.Species
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class PetListViewModel(
    petRepository: PetRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(PetFilter())
    val filter: StateFlow<PetFilter> = _filter.asStateFlow()

    private val _showFilterDialog = MutableStateFlow(false)
    val showFilterDialog: StateFlow<Boolean> = _showFilterDialog.asStateFlow()

    val uiState: StateFlow<UiState<List<Pet>>> =
        combine(petRepository.observePets(), _filter) { pets, filter ->
            val result: UiState<List<Pet>> = UiState.Success(pets.filter { filter.matches(it) })
            result
        }
            .catch { emit(UiState.Error(R.string.error_generic)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun onQueryChange(query: String) {
        _filter.update { it.copy(query = query) }
    }

    /** Chip selection. Pass null for "All". */
    fun onSpeciesChipSelected(species: Species?) {
        _filter.update {
            it.copy(species = if (species == null) emptySet() else setOf(species))
        }
    }

    fun applyFilter(newFilter: PetFilter) {
        _filter.update { newFilter.copy(query = it.query) }
        _showFilterDialog.value = false
    }

    fun clearFilters() {
        _filter.update { PetFilter(query = it.query) }
    }

    fun openFilterDialog() {
        _showFilterDialog.value = true
    }

    fun closeFilterDialog() {
        _showFilterDialog.value = false
    }
}