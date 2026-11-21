package com.example.proyecto

import com.example.proyecto.domain.model.Pet

interface OnPetClickListener {
    fun onPetClick(pet: Pet)
    fun onPetLongClick(pet: Pet)
}