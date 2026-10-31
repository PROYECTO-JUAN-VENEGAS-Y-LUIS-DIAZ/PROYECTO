package com.example.proyecto

import android.app.Application
import com.example.proyecto.data.memory.MemoryDataRepository
import com.example.proyecto.viewmodel.AdoptaPetViewModelFactory

class AdoptaPetApplication : Application() {

    val repository: MemoryDataRepository by lazy { MemoryDataRepository(this) }

    val viewModelFactory: AdoptaPetViewModelFactory by lazy {
        AdoptaPetViewModelFactory(
            petRepository = repository,
            shelterRepository = repository
        )
    }
}