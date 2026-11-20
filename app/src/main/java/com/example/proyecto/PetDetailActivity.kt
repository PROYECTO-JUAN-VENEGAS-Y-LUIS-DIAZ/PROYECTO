package com.example.proyecto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyecto.util.EXTRA_PET_ID
import com.example.proyecto.util.labelRes
import com.example.proyecto.util.util
import com.example.proyecto.viewmodel.PetViewModel
import kotlinx.coroutines.launch

class PetDetailActivity : AppCompatActivity() {

    private val petViewModel: PetViewModel by viewModels {
        PetViewModel.Factory(application)
    }

    private lateinit var flPhoto: FrameLayout
    private lateinit var tvEmoji: TextView
    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvInfo: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvShelterName: TextView
    private lateinit var tvShelterPhone: TextView
    private var petId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pet_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val receivedId = intent.getStringExtra(EXTRA_PET_ID)?.toLongOrNull()
        if (receivedId == null) {
            finish()
            return
        }
        petId = receivedId

        flPhoto = findViewById(R.id.flPhoto_PetDetail)
        tvEmoji = findViewById(R.id.tvEmoji_PetDetail)
        tvName = findViewById(R.id.tvName_PetDetail)
        tvStatus = findViewById(R.id.tvStatus_PetDetail)
        tvInfo = findViewById(R.id.tvInfo_PetDetail)
        tvDescription = findViewById(R.id.tvDescription_PetDetail)
        tvShelterName = findViewById(R.id.tvShelterName_PetDetail)
        tvShelterPhone = findViewById(R.id.tvShelterPhone_PetDetail)

        val btnEdit = findViewById<Button>(R.id.btnEdit_PetDetail)
        val btnDelete = findViewById<Button>(R.id.btnDelete_PetDetail)

        tvShelterPhone.setOnClickListener(View.OnClickListener { view ->
            val phone = tvShelterPhone.text.toString().replace(" ", "")
            if (phone.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            }
        })

        btnEdit.setOnClickListener(View.OnClickListener { view ->
            util.openActivity(
                this,
                PetFormActivity::class.java,
                EXTRA_PET_ID, petId.toString()
            )
        })

        btnDelete.setOnClickListener(View.OnClickListener { view ->
            util.showDialogCondition(
                this,
                getString(R.string.question_delete_pet)
            ) { deletePet() }
        })
    }

    override fun onResume() {
        super.onResume()
        if (petId != 0L) loadPet()
    }

    private fun loadPet() {
        lifecycleScope.launch {
            try {
                val pet = petViewModel.getPetById(petId)
                if (pet != null) {
                    val shelter = petViewModel.getShelterById(pet.shelterId)
                    flPhoto.setBackgroundColor(
                        ContextCompat.getColor(this@PetDetailActivity, util.getSpeciesColor(pet.species))
                    )
                    tvEmoji.setText(util.getSpeciesEmoji(pet.species))
                    tvName.text = pet.name
                    tvStatus.setText(pet.status.labelRes())
                    tvInfo.text = util.formatPetSummary(this@PetDetailActivity, pet)
                    tvDescription.text = pet.description
                    tvShelterName.text = shelter?.name.orEmpty()
                    tvShelterPhone.text = shelter?.phone.orEmpty()
                } else {
                    Toast.makeText(
                        this@PetDetailActivity,
                        getString(R.string.error_pet_not_found),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PetDetailActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun deletePet() {
        val progressDialog = util.showProgressDialog(this, getString(R.string.text_deleting))
        lifecycleScope.launch {
            try {
                petViewModel.removePet(petId)
                Toast.makeText(
                    this@PetDetailActivity,
                    getString(R.string.message_pet_deleted),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@PetDetailActivity,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }
}