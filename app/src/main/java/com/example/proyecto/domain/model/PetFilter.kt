package com.example.proyecto.domain.model

data class PetFilter(
    val query: String = "",
    val species: Set<Species> = emptySet(),
    val sizes: Set<PetSize> = emptySet(),
    val statuses: Set<AdoptionStatus> = emptySet(),
    val breed: String = ""
) {
    val hasActiveFilters: Boolean
        get() = species.isNotEmpty() || sizes.isNotEmpty() ||
                statuses.isNotEmpty() || breed.isNotBlank()

    fun matches(pet: Pet): Boolean {
        val cleanQuery = query.trim()
        val cleanBreed = breed.trim()
        return (cleanQuery.isEmpty() || pet.name.contains(cleanQuery, ignoreCase = true)) &&
                (species.isEmpty() || pet.species in species) &&
                (sizes.isEmpty() || pet.size in sizes) &&
                (statuses.isEmpty() || pet.status in statuses) &&
                (cleanBreed.isEmpty() || pet.breed.contains(cleanBreed, ignoreCase = true))
    }
}