package com.example.proyecto

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.util.util

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnExplore: Button = findViewById<Button>(R.id.btnExplore_main)
        btnExplore.setOnClickListener(View.OnClickListener { view ->
            util.openActivity(this
                , PetListActivity::class.java)
        })

        val btnRegister: Button = findViewById<Button>(R.id.btnRegister_main)
        btnRegister.setOnClickListener(View.OnClickListener { view ->
            util.openActivity(this
                , PetFormActivity::class.java)
        })

        val btnLogin: Button = findViewById<Button>(R.id.btnLogin_main)
        btnLogin.setOnClickListener(View.OnClickListener { view ->
            Toast.makeText(this,
                getString(R.string.login_not_available),
                Toast.LENGTH_LONG).show()
        })
    }
}