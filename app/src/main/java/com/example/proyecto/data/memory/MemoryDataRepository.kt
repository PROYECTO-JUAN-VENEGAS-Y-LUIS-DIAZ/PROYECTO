package com.example.proyecto.data.memory

import android.content.Context
import androidx.annotation.RawRes
import com.example.proyecto.R
import com.example.proyecto.domain.model.AdoptionStatus
import com.example.proyecto.domain.model.GeoPoint
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.PetSize
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.domain.model.Species
import com.example.proyecto.domain.repository.PetRepository
import com.example.proyecto.domain.repository.ShelterRepository
import java.util.concurrent.atomic.AtomicLong
import org.json.JSONArray

class MemoryDataRepository(context: Context) : PetRepository, ShelterRepository {

    private val lock = Any()
    private val pets: MutableList<Pet> = loadPets(context).toMutableList()
    private val shelters: List<Shelter> = loadShelters(context)
    private val nextId = AtomicLong((pets.maxOfOrNull { it.id } ?: 0L) + 1L)

    override suspend fun getPets(): List<Pet> = synchronized(lock) { pets.toList() }

    override suspend fun getPetById(id: Long): Pet? =
        synchronized(lock) { pets.firstOrNull { it.id == id } }

    override suspend fun addPet(pet: Pet): Long {
        val newId = nextId.getAndIncrement()
        synchronized(lock) { pets.add(pet.copy(id = newId)) }
        return newId
    }

    override suspend fun updatePet(pet: Pet) {
        synchronized(lock) {
            val index = pets.indexOfFirst { it.id == pet.id }
            if (index != -1) pets[index] = pet
        }
    }

    override suspend fun deletePet(id: Long) {
        synchronized(lock) { pets.removeAll { it.id == id } }
    }

    override suspend fun getShelters(): List<Shelter> = shelters

    override suspend fun getShelterById(id: Long): Shelter? =
        shelters.firstOrNull { it.id == id }
    
    private fun Context.readRaw(@RawRes resId: Int): String =
        resources.openRawResource(resId).bufferedReader().use { it.readText() }

    private fun loadShelters(context: Context): List<Shelter> {
        val array = JSONArray(context.readRaw(R.raw.shelters))
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            Shelter(
                item.getLong("id"),
                item.getString("name"),
                item.getString("phone"),
                item.getString("address"),
                GeoPoint(item.getDouble("latitude"), item.getDouble("longitude"))
            )
        }
    }

    private fun loadPets(context: Context): List<Pet> {
        val array = JSONArray(context.readRaw(R.raw.pets))
        return List(array.length()) { i ->
            val item = array.getJSONObject(i)
            Pet(
                id = item.getLong("id"),
                name = item.getString("name"),
                species = Species.valueOf(item.getString("species")),
                ageYears = item.getInt("ageYears"),
                size = PetSize.valueOf(item.getString("size")),
                breed = item.optString("breed", ""),
                description = item.getString("description"),
                status = AdoptionStatus.valueOf(item.getString("status")),
                shelterId = item.getLong("shelterId")
            )
        }
    }
}