package com.example.proyecto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.GeoPoint
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.domain.repository.ShelterRepository
import com.example.proyecto.util.AppConstants
import com.example.proyecto.util.DistanceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ShelterWithDistance(
    val shelter: Shelter,
    val distanceKm: Double
)

data class ShelterMapUiState(
    val isLoading: Boolean = true,
    val userLocation: GeoPoint = AppConstants.DEFAULT_USER_LOCATION,
    val shelters: List<ShelterWithDistance> = emptyList(),
    val selected: ShelterWithDistance? = null
)

class ShelterMapViewModel(
    shelterRepository: ShelterRepository
) : ViewModel() {

    private val userLocation = MutableStateFlow(AppConstants.DEFAULT_USER_LOCATION)
    private val selectedShelterId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<ShelterMapUiState> =
        combine(
            shelterRepository.observeShelters(),
            userLocation,
            selectedShelterId
        ) { shelters, location, selectedId ->
            val sorted = shelters
                .map { ShelterWithDistance(it, DistanceUtils.haversineKm(location, it.location)) }
                .sortedBy { it.distanceKm }
            ShelterMapUiState(
                isLoading = false,
                userLocation = location,
                shelters = sorted,
                selected = sorted.firstOrNull { it.shelter.id == selectedId } ?: sorted.firstOrNull()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShelterMapUiState())

    fun onUserLocationChanged(location: GeoPoint) {
        userLocation.value = location
    }

    fun onShelterSelected(shelterId: Long) {
        selectedShelterId.value = shelterId
    }
}