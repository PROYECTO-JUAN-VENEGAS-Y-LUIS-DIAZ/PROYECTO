package com.example.proyecto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.util.AppConstants
import com.example.proyecto.util.DistanceUtils
import com.example.proyecto.util.EXTRA_SHELTER_ID
import com.example.proyecto.util.util
import com.example.proyecto.viewmodel.ShelterViewModel
import kotlinx.coroutines.launch

class ShelterMapActivity : AppCompatActivity(), OnShelterClickListener {

    private val shelterViewModel: ShelterViewModel by viewModels {
        ShelterViewModel.Factory(application)
    }

    private lateinit var shelterAdapter: ShelterListAdapter
    private lateinit var txtSearch: EditText
    private lateinit var tvEmpty: TextView
    private lateinit var cardSelected: LinearLayout
    private lateinit var tvSelectedName: TextView
    private lateinit var tvSelectedDistance: TextView
    private lateinit var tvSelectedAddress: TextView
    private var allShelters: List<Shelter> = emptyList()
    private var selectedShelter: Shelter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shelter_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtSearch = findViewById(R.id.txtSearch_ShelterMap)
        tvEmpty = findViewById(R.id.tvEmpty_ShelterMap)
        cardSelected = findViewById(R.id.cardShelter_ShelterMap)
        tvSelectedName = findViewById(R.id.tvShelterName_ShelterMap)
        tvSelectedDistance = findViewById(R.id.tvShelterDistance_ShelterMap)
        tvSelectedAddress = findViewById(R.id.tvShelterAddress_ShelterMap)

        val btnDetails = findViewById<Button>(R.id.btnDetails_ShelterMap)
        val btnAddShelter = findViewById<Button>(R.id.btnAddShelter_ShelterMap)

        val recycler = findViewById<RecyclerView>(R.id.rvShelters_ShelterMap)
        shelterAdapter = ShelterListAdapter(emptyList(), this)
        recycler.layoutManager = LinearLayoutManager(applicationContext)
        recycler.adapter = shelterAdapter

        txtSearch.doOnTextChanged { text, start, before, count ->
            applyFilter()
        }

        btnDetails.setOnClickListener(View.OnClickListener { view ->
            val shelter = selectedShelter
            if (shelter != null) {
                util.openActivity(
                    this,
                    ShelterFormActivity::class.java,
                    EXTRA_SHELTER_ID, shelter.id.toString()
                )
            }
        })

        btnAddShelter.setOnClickListener(View.OnClickListener { view ->
            util.openActivity(this
                , ShelterFormActivity::class.java)
        })

        val tvBack = findViewById<TextView>(R.id.tvBack_ShelterMap)
        tvBack.setOnClickListener(View.OnClickListener { view ->
            finish()
        })

        util.bindBottomNav(this, R.id.navMap_Nav)
    }

    override fun onResume() {
        super.onResume()
        loadShelters()
    }

    override fun onShelterClick(shelter: Shelter) {
        selectedShelter = shelter
        applyFilter()
    }

    private fun loadShelters() {
        lifecycleScope.launch {
            try {
                allShelters = shelterViewModel.getShelters()
                applyFilter()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ShelterMapActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun applyFilter() {
        val query = txtSearch.text.toString().trim()
        val filtered = allShelters
            .filter {
                query.isEmpty()
                        || it.name.contains(query, ignoreCase = true)
                        || it.address.contains(query, ignoreCase = true)
            }
            .sortedBy {
                DistanceUtils.haversineKm(AppConstants.DEFAULT_USER_LOCATION, it.location)
            }

        val selected = filtered.firstOrNull { it.id == selectedShelter?.id }
            ?: filtered.firstOrNull()
        selectedShelter = selected

        shelterAdapter.updateShelters(filtered, selected?.id)
        tvEmpty.isVisible = filtered.isEmpty()
        cardSelected.isVisible = selected != null

        if (selected != null) {
            tvSelectedName.text = selected.name
            tvSelectedAddress.text = selected.address
            tvSelectedDistance.text = DistanceUtils.formatKm(
                DistanceUtils.haversineKm(AppConstants.DEFAULT_USER_LOCATION, selected.location)
            )
        }
    }
}