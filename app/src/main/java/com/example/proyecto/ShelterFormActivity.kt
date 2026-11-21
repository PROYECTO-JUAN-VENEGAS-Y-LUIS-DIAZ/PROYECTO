package com.example.proyecto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.proyecto.domain.model.GeoPoint
import com.example.proyecto.domain.model.Shelter
import com.example.proyecto.util.EXTRA_SHELTER_ID
import com.example.proyecto.util.util
import com.example.proyecto.viewmodel.ShelterViewModel
import kotlinx.coroutines.launch

class ShelterFormActivity : AppCompatActivity() {

    private val shelterViewModel: ShelterViewModel by viewModels {
        ShelterViewModel.Factory(application)
    }

    private lateinit var tvTitle: TextView
    private lateinit var txtId: EditText
    private lateinit var txtName: EditText
    private lateinit var txtPhone: EditText
    private lateinit var txtAddress: EditText
    private lateinit var txtLatitude: EditText
    private lateinit var txtLongitude: EditText
    private lateinit var btnDelete: Button
    private var isEditMode: Boolean = false
    private var currentShelter: Shelter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_shelter_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTitle = findViewById(R.id.tvTitle_ShelterForm)
        txtId = findViewById(R.id.txtId_ShelterForm)
        txtName = findViewById(R.id.txtName_ShelterForm)
        txtPhone = findViewById(R.id.txtPhone_ShelterForm)
        txtAddress = findViewById(R.id.txtAddress_ShelterForm)
        txtLatitude = findViewById(R.id.txtLatitude_ShelterForm)
        txtLongitude = findViewById(R.id.txtLongitude_ShelterForm)
        btnDelete = findViewById(R.id.btnDelete_ShelterForm)

        val btnSearch = findViewById<ImageButton>(R.id.btnSearch_ShelterForm)
        val btnSave = findViewById<Button>(R.id.btnSave_ShelterForm)
        val btnCancel = findViewById<Button>(R.id.btnCancel_ShelterForm)

        val tvBack = findViewById<TextView>(R.id.tvBack_ShelterForm)
        tvBack.setOnClickListener(View.OnClickListener { view ->
            finish()
        })

        val shelterId = intent.getStringExtra(EXTRA_SHELTER_ID)
        if (!shelterId.isNullOrBlank()) {
            txtId.setText(shelterId)
            searchShelter(shelterId)
        }

        btnSearch.setOnClickListener(View.OnClickListener { view ->
            searchShelter(txtId.text.trim().toString())
        })

        btnSave.setOnClickListener(View.OnClickListener { view ->
            if (isEditMode) {
                util.showDialogCondition(
                    this,
                    getString(R.string.question_update_shelter)
                ) { saveShelter() }
            } else {
                saveShelter()
            }
        })

        btnDelete.setOnClickListener(View.OnClickListener { view ->
            util.showDialogCondition(
                this,
                getString(R.string.question_delete_shelter)
            ) { deleteShelter() }
        })

        btnCancel.setOnClickListener(View.OnClickListener { view ->
            cleanScreen()
        })
    }

    private fun searchShelter(id: String) {
        val shelterId = id.toLongOrNull()
        if (shelterId == null) {
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
                val shelter = shelterViewModel.getShelterById(shelterId)
                if (shelter != null) {
                    currentShelter = shelter
                    isEditMode = true
                    btnDelete.isVisible = true
                    txtId.isEnabled = false
                    tvTitle.setText(R.string.shelter_edit_title)
                    txtName.setText(shelter.name)
                    txtPhone.setText(shelter.phone)
                    txtAddress.setText(shelter.address)
                    txtLatitude.setText(shelter.location.latitude.toString())
                    txtLongitude.setText(shelter.location.longitude.toString())
                } else {
                    Toast.makeText(
                        this@ShelterFormActivity,
                        getString(R.string.error_shelter_not_found),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                cleanScreen()
                Toast.makeText(
                    this@ShelterFormActivity,
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
        currentShelter = null
        btnDelete.isVisible = false
        txtId.isEnabled = true
        tvTitle.setText(R.string.shelter_register_title)
        txtId.setText("")
        txtName.setText("")
        txtPhone.setText("")
        txtAddress.setText("")
        txtLatitude.setText("")
        txtLongitude.setText("")
    }

    private fun isValidationData(): Boolean {
        return txtName.text.toString().trim().isNotBlank()
                && txtPhone.text.toString().trim().isNotBlank()
                && txtAddress.text.toString().trim().isNotBlank()
                && txtLatitude.text.toString().trim().toDoubleOrNull() != null
                && txtLongitude.text.toString().trim().toDoubleOrNull() != null
    }

    private fun saveShelter() {
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
                val shelter = Shelter(
                    currentShelter?.id ?: 0L,
                    txtName.text.toString().trim(),
                    txtPhone.text.toString().trim(),
                    txtAddress.text.toString().trim(),
                    GeoPoint(
                        txtLatitude.text.toString().trim().toDouble(),
                        txtLongitude.text.toString().trim().toDouble()
                    )
                )

                if (!isEditMode) {
                    val newId = shelterViewModel.saveShelter(shelter)
                    cleanScreen()
                    Toast.makeText(
                        this@ShelterFormActivity,
                        getString(R.string.message_shelter_created, newId),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    shelterViewModel.updateShelter(shelter)
                    cleanScreen()
                    Toast.makeText(
                        this@ShelterFormActivity,
                        getString(R.string.message_shelter_updated),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ShelterFormActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun deleteShelter() {
        val id = currentShelter?.id ?: return
        val progressDialog = util.showProgressDialog(this, getString(R.string.text_deleting))
        lifecycleScope.launch {
            try {
                shelterViewModel.removeShelter(id)
                cleanScreen()
                Toast.makeText(
                    this@ShelterFormActivity,
                    getString(R.string.message_shelter_deleted),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ShelterFormActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }
}
