package com.example.fireapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fireapp.databinding.ActivityEmergencyBinding

class EmergencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etPhone1.setText("+923267596553")  // Example
    }
}
