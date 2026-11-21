package com.example.proyecto.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.proyecto.MainActivity
import com.example.proyecto.PetFormActivity
import com.example.proyecto.PetListActivity
import com.example.proyecto.R
import com.example.proyecto.ShelterMapActivity
import com.example.proyecto.domain.model.Pet
import com.example.proyecto.domain.model.Species

const val EXTRA_PET_ID = "com.example.proyecto.PetId"
const val EXTRA_SHELTER_ID = "com.example.proyecto.ShelterId"

class util {
    companion object {
        fun openActivity(
            context: Context,
            objClass: Class<*>,
            extraName: String = "",
            value: String? = null
        ) {
            val intent = Intent(context, objClass).apply { putExtra(extraName, value) }
            context.startActivity(intent)
        }

        fun showDialogCondition(context: Context, questionText: String, callback: () -> Unit) {
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setMessage(questionText)
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes)) { _, _ -> callback() }
                .setNegativeButton(context.getString(R.string.no)) { dialog, _ -> dialog.cancel() }

            val alert = dialogBuilder.create()
            alert.setTitle(context.getString(R.string.question_title))
            alert.show()
        }

        fun showProgressDialog(context: Context, message: String): AlertDialog {
            val progressBar = ProgressBar(context).apply {
                isIndeterminate = true
                setPadding(40, 40, 40, 40)
            }
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(50, 40, 50, 40)
                gravity = Gravity.CENTER_VERTICAL
                addView(
                    progressBar,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                val tvMessage = TextView(context).apply {
                    text = message
                    textSize = 16f
                    setPadding(30, 0, 0, 0)
                }
                addView(tvMessage)
            }

            val builder = AlertDialog.Builder(context)
                .setView(container)
                .setCancelable(false)

            val dialog = builder.create()
            dialog.show()
            return dialog
        }

        fun getSpeciesEmoji(species: Species): Int {
            return when (species) {
                Species.DOG -> R.string.emoji_dog
                Species.CAT -> R.string.emoji_cat
                Species.OTHER -> R.string.emoji_other
            }
        }

        fun getSpeciesColor(species: Species): Int {
            return when (species) {
                Species.DOG -> R.color.pet_dog_background
                Species.CAT -> R.color.pet_cat_background
                Species.OTHER -> R.color.detail_photo_background
            }
        }

        fun formatPetSummary(context: Context, pet: Pet): String {
            return context.getString(
                R.string.pet_summary_format,
                context.getString(pet.species.labelRes()),
                context.getString(pet.size.labelRes()),
                context.resources.getQuantityString(R.plurals.age_years, pet.ageYears, pet.ageYears)
            )
        }

        fun bindBottomNav(activity: Activity, selectedItemId: Int) {
            val navHome = activity.findViewById<View>(R.id.navHome_Nav)
            val navCatalog = activity.findViewById<View>(R.id.navCatalog_Nav)
            val navAdd = activity.findViewById<View>(R.id.navAdd_Nav)
            val navMap = activity.findViewById<View>(R.id.navMap_Nav)
            val navProfile = activity.findViewById<View>(R.id.navProfile_Nav)

            listOf(navHome, navCatalog, navAdd, navMap, navProfile).forEach {
                it.alpha = if (it.id == selectedItemId) 1f else 0.55f
            }

            navHome.setOnClickListener(View.OnClickListener { view ->
                val intent = Intent(activity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                activity.startActivity(intent)
            })

            navCatalog.setOnClickListener(View.OnClickListener { view ->
                if (selectedItemId != R.id.navCatalog_Nav) {
                    openActivity(activity, PetListActivity::class.java)
                    activity.finish()
                }
            })

            navAdd.setOnClickListener(View.OnClickListener { view ->
                openActivity(activity, PetFormActivity::class.java)
            })

            navMap.setOnClickListener(View.OnClickListener { view ->
                if (selectedItemId != R.id.navMap_Nav) {
                    openActivity(activity, ShelterMapActivity::class.java)
                    activity.finish()
                }
            })

            navProfile.setOnClickListener(View.OnClickListener { view ->
                Toast.makeText(
                    activity,
                    activity.getString(R.string.profile_not_available),
                    Toast.LENGTH_LONG
                ).show()
            })
        }
    }
}
