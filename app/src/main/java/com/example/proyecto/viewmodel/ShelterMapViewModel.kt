package com.example.proyecto.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.GeoPoint
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.domain.repository.ShelterRepository
import kotlinx.coroutines.launch

data class ShelterUi(
    val shelter: Shelter,
    val distanceKm: Double?
)

data class ShelterMapUiState(
    val shelters: List<ShelterUi> = emptyList(),
    val selected: ShelterUi? = null,
    val query: String = "",
    val userLocation: GeoPoint? = null,
    val isLoading: Boolean = true
)

class ShelterMapViewModel(
    private val shelterRepository: ShelterRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ShelterMapUiState())
    val uiState: LiveData<ShelterMapUiState> = _uiState

    private var allShelters: List<Shelter> = emptyList()
    private var selectedShelterId: Long? = null

    init {
        viewModelScope.launch {
            allShelters = shelterRepository.getShelters()
            publish { it.copy(isLoading = false) }
        }
    }

    fun onQueryChange(query: String) {
        publish { it.copy(query = query) }
    }

    fun onShelterSelected(shelterId: Long?) {
        selectedShelterId = shelterId
        publish { it }
    }

    fun onUserLocationChanged(location: GeoPoint) {
        publish { it.copy(userLocation = location) }
    }

    private fun publish(transform: (ShelterMapUiState) -> ShelterMapUiState) {
        val updated = transform(_uiState.value ?: ShelterMapUiState())
        val text = updated.query.trim()

        val visible = allShelters
            .filter {
                text.isEmpty() ||
                        it.name.contains(text, ignoreCase = true) ||
                        it.address.contains(text, ignoreCase = true)
            }
            .map { ShelterUi(it, distanceTo(updated.userLocation, it.location)) }
            .sortedBy { it.distanceKm ?: Double.MAX_VALUE }

        _uiState.value = updated.copy(
            shelters = visible,
            selected = visible.firstOrNull { it.shelter.id == selectedShelterId }
        )
    }

    private fun distanceTo(from: GeoPoint?, to: GeoPoint): Double? {
        if (from == null) return null
        val results = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results)
        return results[0] / METERS_PER_KM
    }

    private companion object {
        const val METERS_PER_KM = 1000.0
    }
}