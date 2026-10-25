package com.example.proyecto.data.memory

import com.example.proyecto.domain.model.AdoptionStatus
import com.example.proyecto.domain.model.GeoPoint
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.PetSize
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.domain.model.Species
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.domain.repository.ShelterRepository
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MemoryDataRepository : PetRepository, ShelterRepository {

    private val pets = MutableStateFlow(seedPets())
    private val shelters = MutableStateFlow(seedShelters())
    private val nextId = AtomicLong(100L)


    override fun observePets(): Flow<List<Pet>> = pets.asStateFlow()

    override suspend fun getPetById(id: Long): Pet? = pets.value.firstOrNull { it.id == id }

    override suspend fun addPet(pet: Pet): Long {
        val newId = nextId.getAndIncrement()
        pets.update { current -> current + pet.copy(id = newId) }
        return newId
    }

    override suspend fun updatePet(pet: Pet) {
        pets.update { current -> current.map { if (it.id == pet.id) pet else it } }
    }

    override suspend fun deletePet(id: Long) {
        pets.update { current -> current.filterNot { it.id == id } }
    }


    override fun observeShelters(): Flow<List<Shelter>> = shelters.asStateFlow()

    override suspend fun getShelterById(id: Long): Shelter? =
        shelters.value.firstOrNull { it.id == id }


    private fun seedShelters(): List<Shelter> = listOf(
        Shelter(1L, "Refugio Huellas", "+506 8888 8888", "San José, Costa Rica", GeoPoint(9.9281, -84.0907)),
        Shelter(2L, "Patitas Felices", "+506 8777 7777", "Heredia, Costa Rica", GeoPoint(9.9981, -84.1198)),
        Shelter(3L, "Hogar Peludo", "+506 8666 6666", "Cartago, Costa Rica", GeoPoint(9.8644, -83.9194)),
        Shelter(4L, "Amigos Fieles", "+506 8555 5555", "Santa Ana, Costa Rica", GeoPoint(9.9328, -84.1833))
    )

    private fun seedPets(): List<Pet> = listOf(
        Pet(
            id = 1L, name = "Max", species = Species.DOG, ageYears = 2, size = PetSize.MEDIUM,
            breed = "Labrador",
            description = "Max is a very affectionate and playful dog. He loves walking and being around people.",
            status = AdoptionStatus.AVAILABLE, shelterId = 1L
        ),
        Pet(
            id = 2L, name = "Luna", species = Species.CAT, ageYears = 1, size = PetSize.SMALL,
            breed = "Siamese",
            description = "Luna is calm and curious. She is great for apartments.",
            status = AdoptionStatus.AVAILABLE, shelterId = 1L
        ),
        Pet(
            id = 3L, name = "Rocky", species = Species.DOG, ageYears = 4, size = PetSize.LARGE,
            breed = "German Shepherd",
            description = "Rocky is loyal and protective. He needs space and daily exercise.",
            status = AdoptionStatus.IN_PROCESS, shelterId = 2L
        ),
        Pet(
            id = 4L, name = "Misty", species = Species.CAT, ageYears = 3, size = PetSize.SMALL,
            breed = "",
            description = "Misty is shy at first but very loving once she trusts you.",
            status = AdoptionStatus.AVAILABLE, shelterId = 3L
        ),
        Pet(
            id = 5L, name = "Coco", species = Species.OTHER, ageYears = 1, size = PetSize.SMALL,
            breed = "Rabbit",
            description = "Coco is a gentle rabbit who enjoys fresh vegetables and open spaces.",
            status = AdoptionStatus.AVAILABLE, shelterId = 4L
        )
    )
}