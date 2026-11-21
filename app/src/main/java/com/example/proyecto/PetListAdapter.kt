package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.util.labelRes
import com.example.proyecto.util.util

class PetListAdapter(
    private var pets: List<Pet>,
    val itemClickListener: OnPetClickListener
) : RecyclerView.Adapter<PetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val item = pets[position]
        holder.bind(item, itemClickListener)
    }

    override fun getItemCount(): Int {
        return pets.size
    }

    fun updatePets(newPets: List<Pet>) {
        this.pets = newPets
        notifyDataSetChanged()
    }
}

class PetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val flPhoto: FrameLayout = view.findViewById(R.id.flPhoto_Pet)
    val tvEmoji: TextView = view.findViewById(R.id.tvEmoji_Pet)
    val tvStatus: TextView = view.findViewById(R.id.tvStatus_Pet)
    val tvName: TextView = view.findViewById(R.id.tvName_Pet)
    val tvInfo: TextView = view.findViewById(R.id.tvInfo_Pet)

    fun bind(pet: Pet, itemClickListener: OnPetClickListener) {
        flPhoto.setBackgroundColor(
            ContextCompat.getColor(itemView.context, util.getSpeciesColor(pet.species))
        )
        tvEmoji.setText(util.getSpeciesEmoji(pet.species))
        tvStatus.setText(pet.status.labelRes())
        tvName.text = pet.name
        tvInfo.text = util.formatPetSummary(itemView.context, pet)
        itemView.setOnClickListener {
            itemClickListener.onPetClick(pet)
        }
        itemView.setOnLongClickListener {
            itemClickListener.onPetLongClick(pet)
            true
        }
    }
}
