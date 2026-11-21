package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.util.AppConstants
import com.example.proyecto.util.DistanceUtils

class ShelterListAdapter(
    private var shelters: List<Shelter>,
    val itemClickListener: OnShelterClickListener
) : RecyclerView.Adapter<ShelterViewHolder>() {

    private var selectedId: Long? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelterViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shelter, parent, false)
        return ShelterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ShelterViewHolder, position: Int) {
        val item = shelters[position]
        holder.bind(item, item.id == selectedId, itemClickListener)
    }

    override fun getItemCount(): Int {
        return shelters.size
    }

    fun updateShelters(newShelters: List<Shelter>, newSelectedId: Long?) {
        this.shelters = newShelters
        this.selectedId = newSelectedId
        notifyDataSetChanged()
    }
}

class ShelterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvName: TextView = view.findViewById(R.id.tvName_Shelter)
    val tvAddress: TextView = view.findViewById(R.id.tvAddress_Shelter)
    val tvDistance: TextView = view.findViewById(R.id.tvDistance_Shelter)

    fun bind(shelter: Shelter, isSelected: Boolean, itemClickListener: OnShelterClickListener) {
        tvName.text = shelter.name
        tvAddress.text = shelter.address
        tvDistance.text = DistanceUtils.formatKm(
            DistanceUtils.haversineKm(AppConstants.DEFAULT_USER_LOCATION, shelter.location)
        )
        val backgroundColor = if (isSelected) R.color.green_light else R.color.white
        itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, backgroundColor))
        itemView.setOnClickListener {
            itemClickListener.onShelterClick(shelter)
        }
    }
}
