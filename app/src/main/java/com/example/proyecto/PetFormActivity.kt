package com.example.proyecto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.proyecto.domain.model.AdoptionStatus
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.PetSize
import com.example.proyecto.domain.model.Species
import com.example.proyecto.util.AppConstants
import com.example.proyecto.util.EXTRA_PET_ID
import com.example.proyecto.util.util
import com.example.proyecto.viewmodel.PetViewModel
import kotlinx.coroutines.launch

class PetFormActivity : AppCompatActivity() {

    private val petViewModel: PetViewModel by viewModels {
        PetViewModel.Factory(application)
    }

    private lateinit var tvTitle: TextView
    private lateinit var txtId: EditText
    private lateinit var txtName: EditText
    private lateinit var spSpecies: Spinner
    private lateinit var txtAge: EditText
    private lateinit var spSize: Spinner
    private lateinit var txtBreed: EditText
    private lateinit var txtDescription: EditText
    private lateinit var btnDelete: Button
    private var isEditMode: Boolean = false
    private var currentPet: Pet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pet_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTitle = findViewById(R.id.tvTitle_PetForm)
        txtId = findViewById(R.id.txtId_PetForm)
        txtName = findViewById(R.id.txtName_PetForm)
        spSpecies = findViewById(R.id.spSpecies_PetForm)
        txtAge = findViewById(R.id.txtAge_PetForm)
        spSize = findViewById(R.id.spSize_PetForm)
        txtBreed = findViewById(R.id.txtBreed_PetForm)
        txtDescription = findViewById(R.id.txtDescription_PetForm)
        btnDelete = findViewById(R.id.btnDelete_PetForm)

        val btnSearch = findViewById<ImageButton>(R.id.btnSearch_PetForm)
        val btnSave = findViewById<Button>(R.id.btnSave_PetForm)
        val btnCancel = findViewById<Button>(R.id.btnCancel_PetForm)

        val tvBack = findViewById<TextView>(R.id.tvBack_PetForm)
        tvBack.setOnClickListener(View.OnClickListener { view ->
            finish()
        })

        val petId = intent.getStringExtra(EXTRA_PET_ID)
        if (!petId.isNullOrBlank()) {
            txtId.setText(petId)
            searchPet(petId)
        }

        btnSearch.setOnClickListener(View.OnClickListener { view ->
            searchPet(txtId.text.trim().toString())
        })

        btnSave.setOnClickListener(View.OnClickListener { view ->
            if (isEditMode) {
                util.showDialogCondition(
                    this,
                    getString(R.string.question_update_pet)
                ) { savePet() }
            } else {
                savePet()
            }
        })

        btnDelete.setOnClickListener(View.OnClickListener { view ->
            util.showDialogCondition(
                this,
                getString(R.string.question_delete_pet)
            ) { deletePet() }
        })

        btnCancel.setOnClickListener(View.OnClickListener { view ->
            cleanScreen()
        })
    }

    private fun searchPet(id: String) {
        val petId = id.toLongOrNull()
        if (petId == null) {
            Toast.makeText(
                this,
                getString(R.string.error_invalid_id),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val progressDialog = util.showProgressDialog(this, getString(R.string.text_loading))
        lifecycleScope.launch {
            try {
                val pet = petViewModel.getPetById(petId)
                if (pet != null) {
                    currentPet = pet
                    isEditMode = true
                    btnDelete.isVisible = true
                    txtId.isEnabled = false
                    tvTitle.setText(R.string.edit_title)
                    txtName.setText(pet.name)
                    spSpecies.setSelection(pet.species.ordinal + 1)
                    txtAge.setText(pet.ageYears.toString())
                    spSize.setSelection(pet.size.ordinal + 1)
                    txtBreed.setText(pet.breed)
                    txtDescription.setText(pet.description)
                } else {
                    Toast.makeText(
                        this@PetFormActivity,
                        getString(R.string.error_pet_not_found),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                cleanScreen()
                Toast.makeText(
                    this@PetFormActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun cleanScreen() {
        isEditMode = false
        currentPet = null
        btnDelete.isVisible = false
        txtId.isEnabled = true
        tvTitle.setText(R.string.register_title)
        txtId.setText("")
        txtName.setText("")
        spSpecies.setSelection(0)
        txtAge.setText("")
        spSize.setSelection(0)
        txtBreed.setText("")
        txtDescription.setText("")
    }

    private fun isValidationData(): Boolean {
        val age = txtAge.text.toString().trim().toIntOrNull()
        return txtName.text.toString().trim().isNotBlank()
                && spSpecies.selectedItemPosition > 0
                && age != null
                && age in 0..AppConstants.MAX_PET_AGE
                && spSize.selectedItemPosition > 0
                && txtDescription.text.toString().trim().isNotBlank()
    }

    private fun savePet() {
        if (!isValidationData()) {
            Toast.makeText(
                this,
                getString(R.string.error_incomplete_data),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val progressDialog = util.showProgressDialog(this, getString(R.string.text_saving))
        lifecycleScope.launch {
            try {
                val pet = Pet(
                    currentPet?.id ?: 0L,
                    txtName.text.toString().trim(),
                    Species.entries[spSpecies.selectedItemPosition - 1],
                    txtAge.text.toString().trim().toInt(),
                    PetSize.entries[spSize.selectedItemPosition - 1],
                    txtBreed.text.toString().trim(),
                    txtDescription.text.toString().trim(),
                    currentPet?.photoUri,
                    currentPet?.status ?: AdoptionStatus.AVAILABLE,
                    currentPet?.shelterId ?: 0L
                )

                if (!isEditMode) {
                    val newId = petViewModel.savePet(pet)
                    cleanScreen()
                    Toast.makeText(
                        this@PetFormActivity,
                        getString(R.string.message_pet_created, newId),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    petViewModel.updatePet(pet)
                    cleanScreen()
                    Toast.makeText(
                        this@PetFormActivity,
                        getString(R.string.message_pet_updated),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PetFormActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun deletePet() {
        val id = currentPet?.id ?: return
        val progressDialog = util.showProgressDialog(this, getString(R.string.text_deleting))
        lifecycleScope.launch {
            try {
                petViewModel.removePet(id)
                cleanScreen()
                Toast.makeText(
                    this@PetFormActivity,
                    getString(R.string.message_pet_deleted),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@PetFormActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }
}