package com.example.proyecto

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.domain.model.AdoptionStatus
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.PetFilter
import com.example.proyecto.domain.model.PetSize
import com.example.proyecto.domain.model.Species
import com.example.proyecto.util.EXTRA_PET_ID
import com.example.proyecto.util.labelRes
import com.example.proyecto.util.util
import com.example.proyecto.viewmodel.PetViewModel
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class PetListActivity : AppCompatActivity(), OnPetClickListener {

    private val petViewModel: PetViewModel by viewModels {
        PetViewModel.Factory(application)
    }

    private lateinit var petAdapter: PetListAdapter
    private lateinit var txtSearch: EditText
    private lateinit var chipGroup: ChipGroup
    private lateinit var tvSizeValue: TextView
    private lateinit var tvStatusValue: TextView
    private lateinit var tvBreedValue: TextView
    private lateinit var tvEmpty: TextView
    private var allPets: List<Pet> = emptyList()
    private var petFilter: PetFilter = PetFilter()
    private var isUpdatingChips: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pet_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtSearch = findViewById(R.id.txtSearch_PetList)
        chipGroup = findViewById(R.id.chipGroup_PetList)
        tvSizeValue = findViewById(R.id.tvSizeValue_PetList)
        tvStatusValue = findViewById(R.id.tvStatusValue_PetList)
        tvBreedValue = findViewById(R.id.tvBreedValue_PetList)
        tvEmpty = findViewById(R.id.tvEmpty_PetList)

        val recycler = findViewById<RecyclerView>(R.id.rvPets_PetList)
        petAdapter = PetListAdapter(emptyList(), this)
        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = petAdapter

        val rowSize = findViewById<View>(R.id.rowSize_PetList)
        val rowStatus = findViewById<View>(R.id.rowStatus_PetList)
        val rowBreed = findViewById<View>(R.id.rowBreed_PetList)

        txtSearch.doOnTextChanged { text, start, before, count ->
            petFilter = petFilter.copy(query = text.toString())
            applyFilters()
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (!isUpdatingChips) {
                val species = when (checkedIds.firstOrNull()) {
                    R.id.chipDogs_PetList -> Species.DOG
                    R.id.chipCats_PetList -> Species.CAT
                    R.id.chipOthers_PetList -> Species.OTHER
                    else -> null
                }
                petFilter = petFilter.copy(
                    species = if (species == null) emptySet() else setOf(species)
                )
                applyFilters()
            }
        }

        rowSize.setOnClickListener(View.OnClickListener { view ->
            showFilterDialog()
        })

        rowStatus.setOnClickListener(View.OnClickListener { view ->
            showFilterDialog()
        })

        rowBreed.setOnClickListener(View.OnClickListener { view ->
            showFilterDialog()
        })

        val tvBack = findViewById<TextView>(R.id.tvBack_PetList)
        tvBack.setOnClickListener(View.OnClickListener { view ->
            finish()
        })

        util.bindBottomNav(this, R.id.navCatalog_Nav)
    }

    override fun onResume() {
        super.onResume()
        loadPets()
    }

    override fun onPetClick(pet: Pet) {
        util.openActivity(
            this,
            PetDetailActivity::class.java,
            EXTRA_PET_ID, pet.id.toString()
        )
    }

    override fun onPetLongClick(pet: Pet) {
        util.showDialogCondition(
            this,
            getString(R.string.question_delete_pet)
        ) { deletePet(pet) }
    }

    private fun loadPets() {
        lifecycleScope.launch {
            try {
                allPets = petViewModel.getPets()
                applyFilters()
            } catch (e: Exception) {
                Toast.makeText(
                    this@PetListActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun deletePet(pet: Pet) {
        val progressDialog = util.showProgressDialog(this, getString(R.string.text_deleting))
        lifecycleScope.launch {
            try {
                petViewModel.removePet(pet.id)
                allPets = petViewModel.getPets()
                applyFilters()
                Toast.makeText(
                    this@PetListActivity,
                    getString(R.string.message_pet_deleted),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@PetListActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun applyFilters() {
        val filtered = allPets.filter { petFilter.matches(it) }
        petAdapter.updatePets(filtered)
        tvEmpty.isVisible = filtered.isEmpty()

        tvSizeValue.text = summarize(petFilter.sizes.map { getString(it.labelRes()) })
        tvStatusValue.text = summarize(petFilter.statuses.map { getString(it.labelRes()) })
        tvBreedValue.text = summarize(listOfNotNull(petFilter.breed.trim().ifEmpty { null }))

        val targetId = when {
            petFilter.species.isEmpty() -> R.id.chipAll_PetList
            petFilter.species.size == 1 -> when (petFilter.species.first()) {
                Species.DOG -> R.id.chipDogs_PetList
                Species.CAT -> R.id.chipCats_PetList
                Species.OTHER -> R.id.chipOthers_PetList
            }
            else -> View.NO_ID
        }
        isUpdatingChips = true
        if (targetId == View.NO_ID) chipGroup.clearCheck() else chipGroup.check(targetId)
        isUpdatingChips = false
    }

    private fun summarize(labels: List<String>): String {
        return if (labels.isEmpty()) getString(R.string.filter_all)
        else labels.joinToString(", ")
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filters, null)

        val cbDog = dialogView.findViewById<CheckBox>(R.id.cbDog_Filters)
        val cbCat = dialogView.findViewById<CheckBox>(R.id.cbCat_Filters)
        val cbOther = dialogView.findViewById<CheckBox>(R.id.cbOther_Filters)
        val cbSmall = dialogView.findViewById<CheckBox>(R.id.cbSmall_Filters)
        val cbMedium = dialogView.findViewById<CheckBox>(R.id.cbMedium_Filters)
        val cbLarge = dialogView.findViewById<CheckBox>(R.id.cbLarge_Filters)
        val cbAvailable = dialogView.findViewById<CheckBox>(R.id.cbAvailable_Filters)
        val cbInProcess = dialogView.findViewById<CheckBox>(R.id.cbInProcess_Filters)
        val btnClear = dialogView.findViewById<Button>(R.id.btnClear_Filters)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApply_Filters)
        val tvClose = dialogView.findViewById<TextView>(R.id.tvClose_Filters)

        cbDog.isChecked = Species.DOG in petFilter.species
        cbCat.isChecked = Species.CAT in petFilter.species
        cbOther.isChecked = Species.OTHER in petFilter.species
        cbSmall.isChecked = PetSize.SMALL in petFilter.sizes
        cbMedium.isChecked = PetSize.MEDIUM in petFilter.sizes
        cbLarge.isChecked = PetSize.LARGE in petFilter.sizes
        cbAvailable.isChecked = AdoptionStatus.AVAILABLE in petFilter.statuses
        cbInProcess.isChecked = AdoptionStatus.IN_PROCESS in petFilter.statuses

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnApply.setOnClickListener(View.OnClickListener { view ->
            val species = buildSet<Species> {
                if (cbDog.isChecked) add(Species.DOG)
                if (cbCat.isChecked) add(Species.CAT)
                if (cbOther.isChecked) add(Species.OTHER)
            }
            val sizes = buildSet<PetSize> {
                if (cbSmall.isChecked) add(PetSize.SMALL)
                if (cbMedium.isChecked) add(PetSize.MEDIUM)
                if (cbLarge.isChecked) add(PetSize.LARGE)
            }
            val statuses = buildSet<AdoptionStatus> {
                if (cbAvailable.isChecked) add(AdoptionStatus.AVAILABLE)
                if (cbInProcess.isChecked) add(AdoptionStatus.IN_PROCESS)
            }
            petFilter = petFilter.copy(species = species, sizes = sizes, statuses = statuses)
            applyFilters()
            dialog.dismiss()
        })

        btnClear.setOnClickListener(View.OnClickListener { view ->
            petFilter = PetFilter(query = petFilter.query)
            applyFilters()
            dialog.dismiss()
        })

        tvClose.setOnClickListener(View.OnClickListener { view ->
            dialog.dismiss()
        })

        dialog.show()
    }
}