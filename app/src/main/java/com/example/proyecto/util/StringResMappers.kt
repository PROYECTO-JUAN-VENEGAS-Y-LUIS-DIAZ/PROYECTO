package com.example.proyecto.util

import androidx.annotation.StringRes
import com.example.proyecto.R
import com.example.proyecto.domain.model.AdoptionStatus
import com.example.proyecto.domain.model.PetSize
import com.example.proyecto.domain.model.Species

@StringRes
fun Species.labelRes(): Int = when (this) {
    Species.DOG -> R.string.species_dog
    Species.CAT -> R.string.species_cat
    Species.OTHER -> R.string.species_other
}

@StringRes
fun Species.pluralLabelRes(): Int = when (this) {
    Species.DOG -> R.string.species_dogs
    Species.CAT -> R.string.species_cats
    Species.OTHER -> R.string.species_others
}

@StringRes
fun PetSize.labelRes(): Int = when (this) {
    PetSize.SMALL -> R.string.size_small
    PetSize.MEDIUM -> R.string.size_medium
    PetSize.LARGE -> R.string.size_large
}

@StringRes
fun AdoptionStatus.labelRes(): Int = when (this) {
    AdoptionStatus.AVAILABLE -> R.string.status_available
    AdoptionStatus.IN_PROCESS -> R.string.status_in_process
    AdoptionStatus.ADOPTED -> R.string.status_adopted
}