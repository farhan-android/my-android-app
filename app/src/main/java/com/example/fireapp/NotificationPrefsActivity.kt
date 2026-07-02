package com.example.fireapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fireapp.databinding.ActivityNotificationPrefsBinding

class NotificationPrefsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationPrefsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationPrefsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example logic
        binding.cbAlertSound.setOnCheckedChangeListener { _, isChecked ->
            // save to firebase or shared prefs
        }
    }
}
