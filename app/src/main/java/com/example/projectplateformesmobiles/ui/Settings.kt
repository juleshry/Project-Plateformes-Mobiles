package com.example.projectplateformesmobiles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.projectplateformesmobiles.R

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backButton: Button = findViewById(R.id.settings_back_button)
        backButton.setOnClickListener{
            this.finish()
        }
    }
}